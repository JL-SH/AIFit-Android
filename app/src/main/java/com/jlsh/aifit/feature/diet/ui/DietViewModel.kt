package com.jlsh.aifit.feature.diet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.feature.diet.data.dto.GenerateAdaptiveDietPlanRequestDto
import com.jlsh.aifit.feature.diet.data.dto.GenerateDietPlanRequestDto
import com.jlsh.aifit.feature.diet.domain.usecase.DeleteDietPlanUseCase
import com.jlsh.aifit.feature.diet.domain.usecase.GenerateDietPlanUseCase
import com.jlsh.aifit.feature.diet.domain.usecase.GetDietPlanDetailUseCase
import com.jlsh.aifit.feature.diet.ui.state.DietUiEvent
import com.jlsh.aifit.feature.diet.ui.state.DietUiState
import com.jlsh.aifit.feature.diet.ui.state.GenerateDietUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DietViewModel @Inject constructor(
    private val getDietPlanDetailUseCase: GetDietPlanDetailUseCase,
    private val generateDietPlanUseCase: GenerateDietPlanUseCase,
    private val deleteDietPlanUseCase: DeleteDietPlanUseCase,
) : ViewModel() {

    // 1. UI STATE
    private val _detailUiState = MutableStateFlow<DietUiState>(DietUiState.Loading)
    val detailUiState: StateFlow<DietUiState> = _detailUiState.asStateFlow()

    private val _generateUiState = MutableStateFlow<GenerateDietUiState>(GenerateDietUiState.Idle)
    val generateUiState: StateFlow<GenerateDietUiState> = _generateUiState.asStateFlow()

    // 2. EVENTS CHANNEL
    private val _events = Channel<DietUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // 5. PUBLIC FUNCTIONS
    fun loadPlanDetail(planId: String) {
        viewModelScope.launch {
            _detailUiState.value = DietUiState.Loading
            when (val result = getDietPlanDetailUseCase(planId)) {
                is Result.Success -> {
                    _detailUiState.value = DietUiState.Success(plan = result.data)
                }
                is Result.Error -> {
                    _detailUiState.value = DietUiState.Error(result.exception.toMessage())
                }
                else -> Unit
            }
        }
    }

    fun onDeletePlan(planId: String) {
        viewModelScope.launch {
            when (val result = deleteDietPlanUseCase(planId)) {
                is Result.Success -> {
                    emitEvent(DietUiEvent.ShowSnackbar("Plan eliminado"))
                    emitEvent(DietUiEvent.NavigateBack)
                }
                is Result.Error -> {
                    emitEvent(DietUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    fun onGeneratePlan(request: GenerateDietPlanRequestDto) {
        viewModelScope.launch {
            _generateUiState.value = GenerateDietUiState.Generating
            val startTime = System.currentTimeMillis()

            when (val result = generateDietPlanUseCase(request)) {
                is Result.Success -> {
                    ensureMinAnimationDuration(startTime)
                    _generateUiState.value = GenerateDietUiState.Success(result.data)
                    emitEvent(DietUiEvent.NavigateToDetail(result.data.id))
                }
                is Result.Error -> {
                    ensureMinAnimationDuration(startTime)
                    _generateUiState.value = GenerateDietUiState.Error(result.exception.toMessage())
                    emitEvent(DietUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    fun onGenerateAdaptivePlan(request: GenerateAdaptiveDietPlanRequestDto) {
        viewModelScope.launch {
            _generateUiState.value = GenerateDietUiState.Generating
            val startTime = System.currentTimeMillis()

            when (val result = generateDietPlanUseCase.invokeAdaptive(request)) {
                is Result.Success -> {
                    ensureMinAnimationDuration(startTime)
                    _generateUiState.value = GenerateDietUiState.Success(result.data)
                    emitEvent(DietUiEvent.NavigateToDetail(result.data.id))
                }
                is Result.Error -> {
                    ensureMinAnimationDuration(startTime)
                    _generateUiState.value = GenerateDietUiState.Error(result.exception.toMessage())
                    emitEvent(DietUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    fun onNavigateToGenerate(adaptive: Boolean, basePlanId: String?) {
        // Navigation handled by screen lambdas — this is a pass-through if needed
    }

    // 6. PRIVATE HELPERS
    private fun emitEvent(event: DietUiEvent) {
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

