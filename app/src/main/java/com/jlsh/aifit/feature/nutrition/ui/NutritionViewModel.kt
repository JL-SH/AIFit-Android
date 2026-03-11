package com.jlsh.aifit.feature.nutrition.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.usecase.GetDietPlansUseCase
import com.jlsh.aifit.feature.nutrition.data.dto.AnalyzeMealFromTextRequestDto
import com.jlsh.aifit.feature.nutrition.data.dto.TrackMealRequestDto
import com.jlsh.aifit.feature.nutrition.data.dto.UpdateNutritionTargetRequestDto
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionLog
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget
import com.jlsh.aifit.feature.nutrition.domain.model.TargetSource
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val getNutritionLogUseCase: GetNutritionLogUseCase,
    private val getCurrentNutritionTargetUseCase: GetCurrentNutritionTargetUseCase,
    private val getDietPlansUseCase: GetDietPlansUseCase,
    private val trackMealUseCase: TrackMealUseCase,
    private val analyzeMealFromTextUseCase: AnalyzeMealFromTextUseCase,
    private val deleteMealLogUseCase: DeleteMealLogUseCase,
    private val updateNutritionTargetUseCase: UpdateNutritionTargetUseCase,
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

    init {
        loadHubData()
    }

    // ===== HUB =====

    private fun loadHubData() {
        viewModelScope.launch {
            combine(
                getNutritionLogUseCase(LocalDate.now()),
                getCurrentNutritionTargetUseCase(),
                getDietPlansUseCase(),
            ) { logResult, targetResult, plansResult ->
                Triple(logResult, targetResult, plansResult)
            }.collect { (logResult, targetResult, plansResult) ->
                val log = (logResult as? Result.Success)?.data
                val target = (targetResult as? Result.Success)?.data
                val plans = (plansResult as? Result.Success)?.data ?: emptyList()

                when {
                    logResult is Result.Loading || targetResult is Result.Loading ->
                        _hubState.value = NutritionHubUiState.Loading

                    log != null && target != null ->
                        _hubState.value = NutritionHubUiState.Success(
                            todayState = TodayState(nutritionLog = log, target = target),
                            dietPlans = plans,
                            selectedTabIndex = _selectedTabIndex.value,
                        )

                    logResult is Result.Error ->
                        _hubState.value = NutritionHubUiState.Error(logResult.exception.toMessage())

                    targetResult is Result.Error ->
                        _hubState.value = NutritionHubUiState.Error(targetResult.exception.toMessage())

                    else ->
                        _hubState.value = NutritionHubUiState.Loading
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

    fun onRefresh() {
        loadHubData()
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
                    emitEvent(NutritionUiEvent.ShowSnackbar("Comida registrada"))
                    emitEvent(NutritionUiEvent.NavigateBack)
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
                    emitEvent(NutritionUiEvent.ShowSnackbar("Comida registrada"))
                    emitEvent(NutritionUiEvent.NavigateBack)
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
                    emitEvent(NutritionUiEvent.ShowSnackbar("Targets updated"))
                    emitEvent(NutritionUiEvent.NavigateBack)
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

