package com.jlsh.aifit.feature.training.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.feature.training.data.dto.GenerateAdaptiveTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.data.dto.GenerateTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import com.jlsh.aifit.feature.training.domain.usecase.DeleteTrainingPlanUseCase
import com.jlsh.aifit.feature.training.domain.usecase.GenerateTrainingPlanUseCase
import com.jlsh.aifit.feature.training.domain.usecase.GetTrainingPlanDetailUseCase
import com.jlsh.aifit.feature.training.domain.usecase.GetTrainingPlansUseCase
import com.jlsh.aifit.feature.training.domain.usecase.SetActivePlanUseCase
import com.jlsh.aifit.feature.training.ui.state.GeneratePlanUiState
import com.jlsh.aifit.feature.training.domain.model.TrainingDayType
import com.jlsh.aifit.feature.training.ui.state.TrainingDayItem
import com.jlsh.aifit.feature.training.ui.state.TrainingDetailUiState
import com.jlsh.aifit.feature.training.ui.state.TrainingHubUiState
import com.jlsh.aifit.feature.training.ui.state.TrainingUiEvent
import com.jlsh.aifit.feature.training.ui.state.TrainingUiState
import com.jlsh.aifit.feature.user.domain.usecase.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class TrainingViewModel @Inject constructor(
    private val getTrainingPlansUseCase: GetTrainingPlansUseCase,
    private val getTrainingPlanDetailUseCase: GetTrainingPlanDetailUseCase,
    private val generateTrainingPlanUseCase: GenerateTrainingPlanUseCase,
    private val deleteTrainingPlanUseCase: DeleteTrainingPlanUseCase,
    private val setActivePlanUseCase: SetActivePlanUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
) : ViewModel() {

    // 1. UI STATE
    private val _uiState = MutableStateFlow<TrainingUiState>(TrainingUiState.Loading)
    val uiState: StateFlow<TrainingUiState> = _uiState.asStateFlow()

    private val _hubUiState = MutableStateFlow<TrainingHubUiState>(TrainingHubUiState.Loading)
    val hubUiState: StateFlow<TrainingHubUiState> = _hubUiState.asStateFlow()

    private val _detailUiState = MutableStateFlow<TrainingDetailUiState>(TrainingDetailUiState.Loading)
    val detailUiState: StateFlow<TrainingDetailUiState> = _detailUiState.asStateFlow()

    private val _generateUiState = MutableStateFlow<GeneratePlanUiState>(GeneratePlanUiState.Idle)
    val generateUiState: StateFlow<GeneratePlanUiState> = _generateUiState.asStateFlow()

    private val _userFitnessLevel = MutableStateFlow("INTERMEDIATE")
    val userFitnessLevel: StateFlow<String> = _userFitnessLevel.asStateFlow()

    // 2. EVENTS CHANNEL
    private val _events = Channel<TrainingUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // 3. LOCAL UI STATE
    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    private var fetchPlansJob: Job? = null
    private var isDeletingPlan = false

    /**
     * Debounced refresh trigger (Option A).
     * Rapid lifecycle transitions (RESUMED → PAUSED → RESUMED during navigation) can fire
     * onRefresh() 10+ times per second. Routing all UI-initiated refreshes through this
     * SharedFlow with a 500 ms debounce collapses bursts into a single fetchPlans() call,
     * while post-mutation refreshes (delete/activate/generate) still call fetchPlans() directly
     * and are unaffected.
     */
    private val _refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // 4. INIT
    init {
        fetchPlans()
        // Debounce guard: collapse rapid successive onRefresh() calls (lifecycle bounces,
        // navigation transitions) into a single fetchPlans() after a 500 ms quiet period.
        viewModelScope.launch {
            _refreshTrigger
                .debounce(500L)
                .collect { fetchPlans() }
        }
        viewModelScope.launch {
            getUserProfileUseCase()
                .first { it !is Result.Loading }
                .let { result ->
                    if (result is Result.Success) {
                        _userFitnessLevel.value = result.data.fitnessLevel?.name ?: "INTERMEDIATE"
                    }
                }
        }
    }

    // 5. PUBLIC FUNCTIONS
    fun onRefresh() {
        // TODO: remove diagnostic log below
        Log.d("AIFIT_PLANS", "onRefresh triggered — isDeletingPlan=$isDeletingPlan")
        if (isDeletingPlan) return
        // Route through debounced trigger instead of calling fetchPlans() directly to
        // collapse rapid lifecycle-bounce events (e.g. repeatOnLifecycle firing 10+ times
        // per session during navigation transitions) into a single network call.
        _refreshTrigger.tryEmit(Unit)
    }

    fun onTabSelected(index: Int) {
        _selectedTabIndex.value = index
    }

    fun onPlanClicked(planId: String) {
        emitEvent(TrainingUiEvent.NavigateToDetail(planId))
    }

    fun onDeletePlan(planId: String) {
        deletePlan(planId)
    }

    fun onActivatePlan(planId: String) {
        Log.d("AIFIT_PLANS", "onActivatePlan START — planId=$planId")

        val previousUiState = _uiState.value
        val previousHubState = _hubUiState.value

        val currentSuccess = previousUiState as? TrainingUiState.Success
        if (currentSuccess != null) {
            val optimisticPlans = currentSuccess.plans.map { plan ->
                when {
                    plan.id == planId -> plan.copy(status = PlanStatus.ACTIVE)
                    plan.status == PlanStatus.ACTIVE -> plan.copy(status = PlanStatus.PAUSED)
                    else -> plan
                }
            }
            _uiState.value = currentSuccess.copy(
                plans = optimisticPlans,
                activePlan = optimisticPlans.firstOrNull { it.status == PlanStatus.ACTIVE },
                isActivatingPlan = true,
            )
            _hubUiState.value = computeHubState(optimisticPlans)
        }

        viewModelScope.launch {
            when (val result = setActivePlanUseCase(planId)) {
                is Result.Success -> {
                    Log.d("AIFIT_PLANS", "onActivatePlan SUCCESS — planId=$planId")
                    fetchPlans()
                    // Clear activation overlay after fetchPlans completes
                    _uiState.update { cur ->
                        if (cur is TrainingUiState.Success) cur.copy(isActivatingPlan = false) else cur
                    }
                }
                is Result.Error -> {
                    _uiState.value = previousUiState
                    _hubUiState.value = previousHubState
                    if (result.exception is AppException.NotFoundException) {
                        Log.d("AIFIT_PLANS", "onActivatePlan ERROR (NotFound) — planId=$planId")
                        emitEvent(TrainingUiEvent.ShowSnackbar("Este plan ya no existe. Actualizando lista..."))
                        fetchPlans()
                    } else {
                        emitEvent(TrainingUiEvent.ShowSnackbar(result.exception.toMessage()))
                    }
                }
                else -> Unit
            }
        }
    }

    fun onStartSession(planId: String) {
        emitEvent(TrainingUiEvent.NavigateToWorkoutLog(planId))
    }

    fun onNavigateToGenerate(adaptive: Boolean = false, basePlanId: String? = null) {
        emitEvent(TrainingUiEvent.NavigateToGenerate(adaptive, basePlanId))
    }

    fun onNavigateToWorkoutHistory() {
        emitEvent(TrainingUiEvent.NavigateToWorkoutHistory)
    }

    fun onApprovePlan(planId: String) {
        viewModelScope.launch {
            _detailUiState.value = TrainingDetailUiState.Loading
            when (val result = setActivePlanUseCase(planId)) {
                is Result.Success -> {
                    fetchPlans()
                    emitEvent(TrainingUiEvent.NavigateBack)
                }
                is Result.Error -> {
                    _detailUiState.value =
                        TrainingDetailUiState.Error(result.exception.toMessage())
                    emitEvent(TrainingUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    fun onRejectPlan(planId: String) {
        viewModelScope.launch {
            _detailUiState.value = TrainingDetailUiState.Loading
            when (val result = deleteTrainingPlanUseCase(planId)) {
                is Result.Success -> {
                    fetchPlans()
                    emitEvent(TrainingUiEvent.NavigateToGenerate())
                }
                is Result.Error -> {
                    _detailUiState.value = TrainingDetailUiState.Error(result.exception.toMessage())
                }
                else -> Unit
            }
        }
    }

    fun onRegenerateApprovalPlan(currentPlanId: String, feedback: String?) {
        viewModelScope.launch {
            _detailUiState.value = TrainingDetailUiState.Regenerating

            // Delete old plan silently
            deleteTrainingPlanUseCase(currentPlanId)

            // Get user profile to build adaptive request
            when (val profileResult = getUserProfileUseCase().first { it !is Result.Loading }) {
                is Result.Success -> {
                    val profile = profileResult.data
                    val request = GenerateAdaptiveTrainingPlanRequestDto(
                        frequencyDaysPerWeek = profile.weeklyWorkoutDays ?: 3,
                        sessionDurationMinutes = profile.availableMinutesPerSession ?: 60,
                        durationWeeks = 8,
                        goalType = profile.goalType?.name ?: "",
                        fitnessLevel = profile.fitnessLevel?.name ?: "",
                        location = profile.workoutLocation?.name ?: "",
                        injuries = profile.injuries,
                        userConsiderations = feedback,
                        includeAthleteHistory = true,
                    )
                    when (val planResult = generateTrainingPlanUseCase.invokeAdaptive(request)) {
                        is Result.Success -> {
                            fetchPlans()
                            emitEvent(TrainingUiEvent.NavigateToApproval(planResult.data.id))
                        }
                        is Result.Error -> {
                            _detailUiState.value = TrainingDetailUiState.Error(planResult.exception.toMessage())
                            emitEvent(TrainingUiEvent.ShowSnackbar(planResult.exception.toMessage()))
                        }
                        else -> Unit
                    }
                }
                is Result.Error -> {
                    _detailUiState.value = TrainingDetailUiState.Error(profileResult.exception.toMessage())
                    emitEvent(TrainingUiEvent.ShowSnackbar(profileResult.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    fun onGeneratePlan(request: GenerateTrainingPlanRequestDto) {
        generatePlan(request)
    }

    fun onGenerateAdaptivePlan(request: GenerateAdaptiveTrainingPlanRequestDto) {
        generateAdaptivePlan(request)
    }

    fun loadPlanDetail(planId: String) {
        // TODO: remove diagnostic log below
        Log.d("AIFIT_DETAIL", "loadPlanDetail START — planId=$planId")
        viewModelScope.launch {
            _detailUiState.value = TrainingDetailUiState.Loading
            when (val result = getTrainingPlanDetailUseCase(planId)) {
                is Result.Success -> {
                    val plan = result.data
                    val days = plan.days.map { day ->
                        if (day.dayType == TrainingDayType.REST || day.exercises.isEmpty()) {
                            TrainingDayItem.Rest(day)
                        } else {
                            TrainingDayItem.Training(day)
                        }
                    }
                    // TODO: remove diagnostic log below
                    Log.d("AIFIT_DETAIL", "loadPlanDetail SUCCESS — planName=${plan.name} daysCount=${days.size}")
                    _detailUiState.value = TrainingDetailUiState.Ready(
                        planName = plan.name,
                        planStatus = plan.status.name,
                        days = days,
                    )
                }
                is Result.Error -> {
                    // TODO: remove diagnostic log below
                    Log.d("AIFIT_DETAIL", "loadPlanDetail ERROR — msg=${result.exception.message}")
                    _detailUiState.value = TrainingDetailUiState.Error(result.exception.toMessage())
                }
                else -> Unit
            }
        }
    }

    fun filterPlans(status: PlanStatus?) {
        val currentState = _hubUiState.value
        if (currentState is TrainingHubUiState.ActivePlan) {
            _hubUiState.value = currentState.copy(selectedFilter = status)
        }
    }

    // 6. PRIVATE HELPERS
    private fun fetchPlans() {
        fetchPlansJob?.cancel()
        fetchPlansJob = viewModelScope.launch {
            // TODO: remove diagnostic log below
            Log.d("AIFIT_PLANS", "fetchPlans STARTED — jobId=${System.identityHashCode(coroutineContext)}")
            // Only show Loading spinner on true first load; silent refresh otherwise
            if (_hubUiState.value is TrainingHubUiState.Loading) {
                _hubUiState.value = TrainingHubUiState.Loading
            }
            if (_uiState.value is TrainingUiState.Loading) {
                _uiState.value = TrainingUiState.Loading
            }
            getTrainingPlansUseCase().collect { result ->
                _uiState.value = when (result) {
                    is Result.Success -> {
                        val plans = result.data
                        // TODO: remove diagnostic log below
                        Log.d("AIFIT_PLANS", "fetchPlans EMISSION SUCCESS — count=${result.data.size}")
                        val active = plans.firstOrNull { it.status == PlanStatus.ACTIVE }
                        _hubUiState.value = computeHubState(plans)
                        TrainingUiState.Success(
                            plans = plans,
                            activePlan = active,
                        )
                    }
                    is Result.Error -> {
                        // TODO: remove diagnostic log below
                        Log.d("AIFIT_PLANS", "fetchPlans EMISSION ERROR — msg=${result.exception.message}")
                        _hubUiState.value = TrainingHubUiState.Error(result.exception.toMessage())
                        TrainingUiState.Error(result.exception.toMessage())
                    }
                    is Result.Loading -> {
                        // Keep current state if data is already visible
                        // TODO: remove diagnostic log below
                        Log.d("AIFIT_PLANS", "fetchPlans EMISSION LOADING — keeping current state")
                        if (_hubUiState.value is TrainingHubUiState.ActivePlan ||
                            _hubUiState.value is TrainingHubUiState.NoActivePlan) {
                            _uiState.value
                        } else {
                            _hubUiState.value = TrainingHubUiState.Loading
                            TrainingUiState.Loading
                        }
                    }
                }
            }
        }
    }

    private fun computeHubState(plans: List<TrainingPlan>): TrainingHubUiState {
        val activePlan = plans.firstOrNull { it.status == PlanStatus.ACTIVE }
            ?: return TrainingHubUiState.NoActivePlan

        val today = LocalDate.now()
        val startDate = activePlan.createdAt.toLocalDate()
        val weeksElapsed = ChronoUnit.WEEKS.between(startDate, today).toInt() + 1
        val currentWeek = weeksElapsed.coerceIn(1, activePlan.durationWeeks)
        val nextDay = activePlan.days.firstOrNull()

        return TrainingHubUiState.ActivePlan(
            plan = activePlan,
            currentWeek = currentWeek,
            nextDay = nextDay,
            allPlans = plans,
            selectedFilter = null,
        )
    }

    private fun deletePlan(planId: String) {
        // TODO: remove diagnostic log below
        Log.d("AIFIT_DELETE", "deletePlan START — planId=$planId isDeletingPlan=$isDeletingPlan")
        isDeletingPlan = true
        viewModelScope.launch {
            // Optimistic UI update: remove the plan instantly before the network call
            val previousHubState = _hubUiState.value
            val previousUiState = _uiState.value
            if (previousHubState is TrainingHubUiState.ActivePlan) {
                val filteredPlans = previousHubState.allPlans.filter { it.id != planId }
                _hubUiState.value = computeHubState(filteredPlans)
                if (previousUiState is TrainingUiState.Success) {
                    val filteredUiPlans = previousUiState.plans.filter { it.id != planId }
                    _uiState.value = previousUiState.copy(
                        plans = filteredUiPlans,
                        activePlan = filteredUiPlans.firstOrNull { it.status == PlanStatus.ACTIVE },
                    )
                }
            }

            // TODO: remove diagnostic log below
            Log.d("AIFIT_DELETE", "Executing deleteUseCase — planId=$planId")
            when (val result = deleteTrainingPlanUseCase(planId)) {
                is Result.Success -> {
                    // TODO: remove diagnostic log below
                    Log.d("AIFIT_DELETE", "deletePlan SUCCESS — planId=$planId")
                    emitEvent(TrainingUiEvent.ShowSnackbar("Plan eliminado"))
                    emitEvent(TrainingUiEvent.PlanDeleted)
                    fetchPlans()
                    // Keep isDeletingPlan=true until the post-delete sync job finishes.
                    // Without this join(), isDeletingPlan resets to false while the new
                    // fetchPlans Job is still running, letting lifecycle-triggered onRefresh
                    // calls fire concurrent network fetches that can race with the server
                    // delete commit and reinsert the plan.
                    fetchPlansJob?.join()
                }
                is Result.Error -> {
                    // TODO: remove diagnostic log below
                    Log.d("AIFIT_DELETE", "deletePlan ERROR — planId=$planId msg=${result.exception.message}")
                    // Restore previous state on failure
                    _hubUiState.value = previousHubState
                    _uiState.value = previousUiState
                    emitEvent(TrainingUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
            isDeletingPlan = false
            // TODO: remove diagnostic log below
            Log.d("AIFIT_DELETE", "deletePlan FINISHED — isDeletingPlan reset to false")
        }
    }

    private fun generatePlan(request: GenerateTrainingPlanRequestDto) {
        viewModelScope.launch {
            _generateUiState.value = GeneratePlanUiState.Loading
            val startTime = System.currentTimeMillis()

            when (val result = generateTrainingPlanUseCase(request)) {
                is Result.Success -> {
                    val elapsed = System.currentTimeMillis() - startTime
                    if (elapsed < MIN_ANIMATION_DURATION) {
                        delay(MIN_ANIMATION_DURATION - elapsed)
                    }
                    // TODO: remove diagnostic log below
                    Log.d("AIFIT_GENERATE", "Generation SUCCESS — planId=${result.data.id} elapsed=$elapsed ms")
                    _generateUiState.value = GeneratePlanUiState.Success(result.data)
                    emitEvent(TrainingUiEvent.NavigateToDetail(result.data.id))
                    fetchPlans()
                }
                is Result.Error -> {
                    val elapsed = System.currentTimeMillis() - startTime
                    if (elapsed < MIN_ANIMATION_DURATION) {
                        delay(MIN_ANIMATION_DURATION - elapsed)
                    }
                    // TODO: remove diagnostic log below
                    Log.d("AIFIT_GENERATE", "Generation ERROR — msg=${result.exception.message} elapsed=$elapsed ms")
                    _generateUiState.value = GeneratePlanUiState.Error(result.exception.toMessage())
                    emitEvent(TrainingUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    private fun generateAdaptivePlan(request: GenerateAdaptiveTrainingPlanRequestDto) {
        viewModelScope.launch {
            _generateUiState.value = GeneratePlanUiState.Loading
            val startTime = System.currentTimeMillis()

            when (val result = generateTrainingPlanUseCase.invokeAdaptive(request)) {
                is Result.Success -> {
                    val elapsed = System.currentTimeMillis() - startTime
                    if (elapsed < MIN_ANIMATION_DURATION) {
                        delay(MIN_ANIMATION_DURATION - elapsed)
                    }
                    // TODO: remove diagnostic log below
                    Log.d("AIFIT_GENERATE", "Generation SUCCESS — planId=${result.data.id} elapsed=$elapsed ms")
                    _generateUiState.value = GeneratePlanUiState.Success(result.data)
                    emitEvent(TrainingUiEvent.NavigateToDetail(result.data.id))
                    fetchPlans()
                }
                is Result.Error -> {
                    val elapsed = System.currentTimeMillis() - startTime
                    if (elapsed < MIN_ANIMATION_DURATION) {
                        delay(MIN_ANIMATION_DURATION - elapsed)
                    }
                    // TODO: remove diagnostic log below
                    Log.d("AIFIT_GENERATE", "Generation ERROR — msg=${result.exception.message} elapsed=$elapsed ms")
                    _generateUiState.value = GeneratePlanUiState.Error(result.exception.toMessage())
                    emitEvent(TrainingUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    private fun emitEvent(event: TrainingUiEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    companion object {
        private const val MIN_ANIMATION_DURATION = 2000L
    }
}
