package com.jlsh.aifit.feature.nutrition.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.feature.diet.domain.usecase.DeleteDietPlanUseCase
import com.jlsh.aifit.feature.diet.domain.usecase.GetDietPlansUseCase
import com.jlsh.aifit.feature.diet.domain.usecase.SetActiveDietPlanUseCase
import com.jlsh.aifit.feature.nutrition.data.dto.AnalyzeMealFromTextRequestDto
import com.jlsh.aifit.feature.nutrition.data.dto.TrackMealRequestDto
import com.jlsh.aifit.feature.nutrition.data.dto.UpdateNutritionTargetRequestDto
import com.jlsh.aifit.feature.nutrition.domain.usecase.AnalyzeMealFromTextUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.DeleteMealLogUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.GetCurrentNutritionTargetUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.GetNutritionLogUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.TrackMealUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.UpdateNutritionTargetUseCase
import com.jlsh.aifit.feature.nutrition.ui.state.NutritionHubUiState
import com.jlsh.aifit.feature.nutrition.ui.state.NutritionTargetUiState
import com.jlsh.aifit.feature.nutrition.ui.state.NutritionUiEvent
import com.jlsh.aifit.feature.nutrition.ui.state.TodayState
import com.jlsh.aifit.feature.nutrition.ui.state.TrackMealUiState
import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val getNutritionLogUseCase: GetNutritionLogUseCase,
    private val getCurrentNutritionTargetUseCase: GetCurrentNutritionTargetUseCase,
    private val getDietPlansUseCase: GetDietPlansUseCase,
    private val trackMealUseCase: TrackMealUseCase,
    private val analyzeMealFromTextUseCase: AnalyzeMealFromTextUseCase,
    private val deleteMealLogUseCase: DeleteMealLogUseCase,
    private val updateNutritionTargetUseCase: UpdateNutritionTargetUseCase,
    private val setActiveDietPlanUseCase: SetActiveDietPlanUseCase,
    private val deleteDietPlanUseCase: DeleteDietPlanUseCase,
) : ViewModel() {

    private val _hubState = MutableStateFlow<NutritionHubUiState>(NutritionHubUiState.Loading)
    val hubState: StateFlow<NutritionHubUiState> = _hubState.asStateFlow()

    private val _trackMealState = MutableStateFlow<TrackMealUiState>(TrackMealUiState.Idle)
    val trackMealState: StateFlow<TrackMealUiState> = _trackMealState.asStateFlow()

    private val _targetState = MutableStateFlow<NutritionTargetUiState>(NutritionTargetUiState.Loading)
    val targetState: StateFlow<NutritionTargetUiState> = _targetState.asStateFlow()

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    private val _events = Channel<NutritionUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var hubLoadJob: Job? = null
    private var isDeletingPlan = false

    private val _refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    init {
        loadHubData()
        viewModelScope.launch {
            _refreshTrigger
                .debounce(500L)
                .collect { loadHubData() }
        }
    }

    // ===== HUB =====

    private fun loadHubData() {
        hubLoadJob?.cancel()
        hubLoadJob = viewModelScope.launch {
            // Step 1: Load log and target as one-shot values in parallel
            val logDeferred = async {
                getNutritionLogUseCase(LocalDate.now())
                    .first { it !is Result.Loading }
                    .let { r -> if (r is Result.Success) r.data else null }
            }
            val targetDeferred = async {
                getCurrentNutritionTargetUseCase()
                    .first { it !is Result.Loading }
                    .let { r -> if (r is Result.Success) r.data else null }
            }
            // Get the first non-Loading diet plans emission in parallel
            val dietPlansDeferred = async {
                getDietPlansUseCase()
                    .first { it !is Result.Loading }
                    .let { r -> if (r is Result.Success) r.data else emptyList() }
            }

            val log = logDeferred.await()
            val target = targetDeferred.await()
            val initialDietPlans = dietPlansDeferred.await()

            // AIFIT_DEBUG — BUG-A: confirmar qué devuelven las llamadas
            Log.d("AIFIT_DEBUG", "[NutritionVM] loadHubData: log=${if (log != null) "id=${log.id} cal=${log.totalCalories} meals=${log.meals.size}" else "NULL"}")
            Log.d("AIFIT_DEBUG", "[NutritionVM] loadHubData: target=${if (target != null) "id=${target.id} cal=${target.calorieTarget} setBy=${target.setBy}" else "NULL"}")
            Log.d("AIFIT_DEBUG", "[NutritionVM] loadHubData: dietPlans=${initialDietPlans.size}")

            // Step 2: Emit initial Success state with all data available
            _hubState.value = NutritionHubUiState.Success(
                todayState = TodayState(nutritionLog = log, target = target),
                dietPlans = initialDietPlans,
                selectedTabIndex = _selectedTabIndex.value,
            )

            // Step 3: Reactively update dietPlans from subsequent emissions
            launch {
                getDietPlansUseCase().drop(1).collect { result ->
                    if (result !is Result.Success) return@collect
                    val current = _hubState.value
                    if (current is NutritionHubUiState.Success) {
                        _hubState.value = current.copy(dietPlans = result.data)
                    }
                }
            }
        }
    }

    fun onTabSelected(index: Int) {
        _selectedTabIndex.value = index
        val current = _hubState.value
        if (current is NutritionHubUiState.Success) {
            _hubState.value = current.copy(selectedTabIndex = index)
        }
    }

    fun onDietPlanFilterChanged(status: com.jlsh.aifit.feature.training.domain.model.PlanStatus?) {
        val current = _hubState.value
        if (current is NutritionHubUiState.Success) {
            _hubState.value = current.copy(selectedDietPlanFilter = status)
        }
    }

    fun onRefresh() {
        if (isDeletingPlan) return
        _refreshTrigger.tryEmit(Unit)
    }

    fun onDeleteMeal(mealId: String) {
        viewModelScope.launch {
            when (val result = deleteMealLogUseCase(mealId)) {
                is Result.Success -> {
                    emitEvent(NutritionUiEvent.MealDeleted)
                    emitEvent(NutritionUiEvent.ShowSnackbar("Comida eliminada"))
                    loadHubData()
                }
                is Result.Error -> emitEvent(NutritionUiEvent.ShowSnackbar(result.exception.toMessage()))
                else -> Unit
            }
        }
    }

    // ===== TRACK MEAL =====

    fun onTrackMeal(request: TrackMealRequestDto) {
        viewModelScope.launch {
            _trackMealState.value = TrackMealUiState.Saving
            when (val result = trackMealUseCase(request)) {
                is Result.Success -> {
                    _trackMealState.value = TrackMealUiState.Saved
                    emitEvent(NutritionUiEvent.NavigateToHome)
                }
                is Result.Error -> {
                    _trackMealState.value = TrackMealUiState.Error(result.exception.toMessage())
                    emitEvent(NutritionUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    fun onAnalyzeMealFromText(request: AnalyzeMealFromTextRequestDto) {
        viewModelScope.launch {
            _trackMealState.value = TrackMealUiState.Analyzing
            val startTime = System.currentTimeMillis()

            when (val result = analyzeMealFromTextUseCase(request)) {
                is Result.Success -> {
                    ensureMinAnimationDuration(startTime)
                    _trackMealState.value = TrackMealUiState.Saved
                    emitEvent(NutritionUiEvent.NavigateToHome)
                }
                is Result.Error -> {
                    ensureMinAnimationDuration(startTime)
                    _trackMealState.value = TrackMealUiState.Error(result.exception.toMessage())
                    emitEvent(NutritionUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    fun resetTrackMealState() {
        _trackMealState.value = TrackMealUiState.Idle
    }

    // ===== NUTRITION TARGET =====

    fun loadNutritionTarget() {
        viewModelScope.launch {
            _targetState.value = NutritionTargetUiState.Loading
            getCurrentNutritionTargetUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        val t = result.data
                        _targetState.value = NutritionTargetUiState.Ready(
                            calorieTarget = t.calorieTarget.toString(),
                            proteinTarget = t.proteinTarget.toInt().toString(),
                            carbsTarget = t.carbsTarget.toInt().toString(),
                            fatTarget = t.fatTarget.toInt().toString(),
                            setBy = t.setBy.name.replace("_", " "),
                        )
                    }
                    is Result.Error -> {
                        _targetState.value = NutritionTargetUiState.Error(result.exception.toMessage())
                    }
                    else -> Unit
                }
            }
        }
    }

    fun onUpdateTarget(calories: String, protein: String, carbs: String, fat: String) {
        viewModelScope.launch {
            val current = _targetState.value
            if (current is NutritionTargetUiState.Ready) {
                _targetState.value = current.copy(isSaving = true)
            }

            val request = UpdateNutritionTargetRequestDto(
                calorieTarget = calories.toIntOrNull(),
                proteinTarget = protein.toDoubleOrNull(),
                carbsTarget = carbs.toDoubleOrNull(),
                fatTarget = fat.toDoubleOrNull(),
            )

            when (val result = updateNutritionTargetUseCase(request)) {
                is Result.Success -> {
                    loadHubData()
                    // NavigateBack MUST be emitted before ShowSnackbar because
                    // showSnackbar() is a suspending call that blocks the UI
                    // collect-loop, preventing subsequent events from being processed.
                    emitEvent(NutritionUiEvent.NavigateBack)
                    emitEvent(NutritionUiEvent.ShowSnackbar("Objetivos actualizados"))
                }
                is Result.Error -> {
                    if (current is NutritionTargetUiState.Ready) {
                        _targetState.value = current.copy(isSaving = false)
                    }
                    emitEvent(NutritionUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    // ===== NAVIGATION =====

    fun onFabClicked() {
        emitEvent(NutritionUiEvent.ShowTrackMealSheet)
    }

    fun onDietPlanClicked(planId: String) {
        emitEvent(NutritionUiEvent.NavigateToDietDetail(planId))
    }

    fun onActivateDietPlan(planId: String) {
        val current = _hubState.value as? NutritionHubUiState.Success ?: return
        val previousPlans = current.dietPlans

        // Optimistic UI update
        val optimisticPlans = previousPlans.map { plan ->
            when {
                plan.id == planId -> plan.copy(status = PlanStatus.ACTIVE)
                plan.status == PlanStatus.ACTIVE -> plan.copy(status = PlanStatus.PAUSED)
                else -> plan
            }
        }
        _hubState.value = current.copy(dietPlans = optimisticPlans, isActivatingPlan = true)

        viewModelScope.launch {
            when (val result = setActiveDietPlanUseCase(planId)) {
                is Result.Success -> {
                    loadHubData()
                    _hubState.value = (_hubState.value as? NutritionHubUiState.Success)
                        ?.copy(isActivatingPlan = false) ?: _hubState.value
                }
                is Result.Error -> {
                    // Roll back
                    _hubState.value = current.copy(isActivatingPlan = false)
                    emitEvent(NutritionUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    fun onDeleteDietPlan(planId: String) {
        val current = _hubState.value as? NutritionHubUiState.Success ?: return
        val plan = current.dietPlans.firstOrNull { it.id == planId } ?: return

        // Do not allow deleting an ACTIVE plan
        if (plan.status == PlanStatus.ACTIVE) {
            emitEvent(NutritionUiEvent.ShowSnackbar("No puedes eliminar un plan activo. Activa otro plan primero."))
            return
        }

        isDeletingPlan = true
        hubLoadJob?.cancel()
        _hubState.value = current.copy(dietPlans = current.dietPlans.filter { it.id != planId })

        viewModelScope.launch {
            when (val result = deleteDietPlanUseCase(planId)) {
                is Result.Success -> {
                    emitEvent(NutritionUiEvent.ShowSnackbar("Plan eliminado"))
                    loadHubData()
                    isDeletingPlan = false
                }
                is Result.Error -> {
                    _hubState.value = current
                    loadHubData()
                    isDeletingPlan = false
                    emitEvent(NutritionUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    fun onGenerateDietClicked() {
        emitEvent(NutritionUiEvent.NavigateToGenerateDiet)
    }

    fun onNavigateToTarget() {
        emitEvent(NutritionUiEvent.NavigateToNutritionTarget)
    }

    // ===== HELPERS =====

    private fun emitEvent(event: NutritionUiEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private suspend fun ensureMinAnimationDuration(startTime: Long) {
        val elapsed = System.currentTimeMillis() - startTime
        if (elapsed < MIN_ANIMATION_DURATION) {
            delay(MIN_ANIMATION_DURATION - elapsed)
        }
    }

    companion object {
        private const val MIN_ANIMATION_DURATION = 2000L
    }
}

