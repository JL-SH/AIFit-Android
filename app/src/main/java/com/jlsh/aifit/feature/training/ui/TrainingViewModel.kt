package com.jlsh.aifit.feature.training.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.jlsh.aifit.feature.training.ui.state.GeneratePlanUiState
import com.jlsh.aifit.feature.training.domain.model.TrainingDayType
import com.jlsh.aifit.feature.training.ui.state.TrainingDayItem
import com.jlsh.aifit.feature.training.ui.state.TrainingDetailUiState
import com.jlsh.aifit.feature.training.ui.state.TrainingHubUiState
import com.jlsh.aifit.feature.training.ui.state.TrainingUiEvent
import com.jlsh.aifit.feature.training.ui.state.TrainingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class TrainingViewModel @Inject constructor(
    private val getTrainingPlansUseCase: GetTrainingPlansUseCase,
    private val getTrainingPlanDetailUseCase: GetTrainingPlanDetailUseCase,
    private val generateTrainingPlanUseCase: GenerateTrainingPlanUseCase,
    private val deleteTrainingPlanUseCase: DeleteTrainingPlanUseCase,
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

    // 2. EVENTS CHANNEL
    private val _events = Channel<TrainingUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // 3. LOCAL UI STATE
    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    // 4. INIT
    init {
        fetchPlans()
    }

    // 5. PUBLIC FUNCTIONS
    fun onRefresh() {
        fetchPlans()
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

    fun onStartSession(planId: String) {
        emitEvent(TrainingUiEvent.NavigateToWorkoutLog(planId))
    }

    fun onNavigateToGenerate(adaptive: Boolean = false, basePlanId: String? = null) {
        emitEvent(TrainingUiEvent.NavigateToGenerate(adaptive, basePlanId))
    }

    fun onGeneratePlan(request: GenerateTrainingPlanRequestDto) {
        generatePlan(request)
    }

    fun onGenerateAdaptivePlan(request: GenerateAdaptiveTrainingPlanRequestDto) {
        generateAdaptivePlan(request)
    }

    fun loadPlanDetail(planId: String) {
        viewModelScope.launch {
            _detailUiState.value = TrainingDetailUiState.Loading
            when (val result = getTrainingPlanDetailUseCase(planId)) {
                is Result.Success -> {
                    val plan = result.data
                    val days = plan.days.map { day ->
                        when (day.dayType) {
                            TrainingDayType.REST -> TrainingDayItem.Rest(day)
                            TrainingDayType.TRAINING -> TrainingDayItem.Training(day)
                        }
                    }
                    _detailUiState.value = TrainingDetailUiState.Ready(
                        planName = plan.name,
                        days = days,
                    )
                }
                is Result.Error -> {
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
        viewModelScope.launch {
            _uiState.value = TrainingUiState.Loading
            _hubUiState.value = TrainingHubUiState.Loading
            getTrainingPlansUseCase().collect { result ->
                _uiState.value = when (result) {
                    is Result.Success -> {
                        val plans = result.data
                        val active = plans.firstOrNull { it.status == PlanStatus.ACTIVE }
                        _hubUiState.value = computeHubState(plans)
                        TrainingUiState.Success(
                            plans = plans,
                            activePlan = active,
                        )
                    }
                    is Result.Error -> {
                        _hubUiState.value = TrainingHubUiState.Error(result.exception.toMessage())
                        TrainingUiState.Error(result.exception.toMessage())
                    }
                    is Result.Loading -> {
                        _hubUiState.value = TrainingHubUiState.Loading
                        TrainingUiState.Loading
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
        viewModelScope.launch {
            when (val result = deleteTrainingPlanUseCase(planId)) {
                is Result.Success -> {
                    emitEvent(TrainingUiEvent.ShowSnackbar("Plan eliminado"))
                    emitEvent(TrainingUiEvent.PlanDeleted)
                    fetchPlans()
                }
                is Result.Error -> {
                    emitEvent(TrainingUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
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
                    _generateUiState.value = GeneratePlanUiState.Success(result.data)
                    emitEvent(TrainingUiEvent.NavigateToDetail(result.data.id))
                    fetchPlans()
                }
                is Result.Error -> {
                    val elapsed = System.currentTimeMillis() - startTime
                    if (elapsed < MIN_ANIMATION_DURATION) {
                        delay(MIN_ANIMATION_DURATION - elapsed)
                    }
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
                    _generateUiState.value = GeneratePlanUiState.Success(result.data)
                    emitEvent(TrainingUiEvent.NavigateToDetail(result.data.id))
                    fetchPlans()
                }
                is Result.Error -> {
                    val elapsed = System.currentTimeMillis() - startTime
                    if (elapsed < MIN_ANIMATION_DURATION) {
                        delay(MIN_ANIMATION_DURATION - elapsed)
                    }
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
