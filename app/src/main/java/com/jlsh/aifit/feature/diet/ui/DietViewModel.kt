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
import com.jlsh.aifit.feature.diet.domain.usecase.SetActiveDietPlanUseCase
import com.jlsh.aifit.feature.diet.ui.state.DietUiEvent
import com.jlsh.aifit.feature.diet.ui.state.DietUiState
import com.jlsh.aifit.feature.diet.ui.state.GenerateDietUiState
import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.user.domain.usecase.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel of the diet flow: plan detail, generation and approval actions.
 *
 * Expone:
 * - [detailUiState]: [DietUiState] for the detail display (loading, success, error, regeneration).
 * - [generateUiState]: [GenerateDietUiState] for the plan generation flow.
 * - [events]: single stream of [DietUiEvent] (navigation, snackbars).
 */
@HiltViewModel
class DietViewModel @Inject constructor(
    private val getDietPlanDetailUseCase: GetDietPlanDetailUseCase,
    private val generateDietPlanUseCase: GenerateDietPlanUseCase,
    private val deleteDietPlanUseCase: DeleteDietPlanUseCase,
    private val setActiveDietPlanUseCase: SetActiveDietPlanUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
) : ViewModel() {

    // 1. UI STATE
    private val _detailUiState = MutableStateFlow<DietUiState>(DietUiState.Loading)

    /** Plan detail observable state ([DietUiState]).*/
    val detailUiState: StateFlow<DietUiState> = _detailUiState.asStateFlow()

    private val _generateUiState = MutableStateFlow<GenerateDietUiState>(GenerateDietUiState.Idle)

    /** Plan generation observable state ([GenerateDietUiState]).*/
    val generateUiState: StateFlow<GenerateDietUiState> = _generateUiState.asStateFlow()

    // 2. EVENTS CHANNEL
    private val _events = Channel<DietUiEvent>(Channel.BUFFERED)

    /**
     * One-time UI events: [DietUiEvent.NavigateBack],
     * [DietUiEvent.NavigateToDetail], [DietUiEvent.NavigateToDietApproval],
     * [DietUiEvent.NavigateToDietGenerate] y [DietUiEvent.ShowSnackbar].
     */
    val events = _events.receiveAsFlow()

    // 5. PUBLIC FUNCTIONS

    /**
     * Load the plan detail and update [detailUiState].
     *
     * @param planId Identifier of the plan to display.
     */
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

    /**
     * Delete the plan if it is not active; casts snackbar and [DietUiEvent.NavigateBack] on success.
     *
     * @param planId Identifier of the plan to delete.
     */
    fun onDeletePlan(planId: String) {
        val current = _detailUiState.value
        if (current is DietUiState.Success && current.plan.status == PlanStatus.ACTIVE) {
            emitEvent(DietUiEvent.ShowSnackbar("No puedes eliminar un plan activo. Activa otro plan primero."))
            return
        }
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

    /**
     * Activate the plan (approval) and navigate back upon completion.
     *
     * @param planId Identifier of the plan to activate.
     */
    fun onApproveDietPlan(planId: String) {
        viewModelScope.launch {
            when (val result = setActiveDietPlanUseCase(planId)) {
                is Result.Success -> {
                    emitEvent(DietUiEvent.PlanApproved)
                }
                is Result.Error -> {
                    _detailUiState.value = DietUiState.Error(result.exception.toMessage())
                    emitEvent(DietUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    /**
     * Reject the plan by deleting it and issue [DietUiEvent.NavigateToDietGenerate] to generate another one.
     *
     * @param planId Identifier of the rejected plan.
     */
    fun onRejectDietPlan(planId: String) {
        viewModelScope.launch {
            when (val result = deleteDietPlanUseCase(planId)) {
                is Result.Success -> {
                    emitEvent(DietUiEvent.NavigateToDietGenerate)
                }
                is Result.Error -> {
                    emitEvent(DietUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    /**
     * Delete the current plan, generate an adaptive one with [feedback] and navigate to approval.
     *
     * @param currentPlanId Plan to replace.
     * @param feedback User considerations for feedback; can be null.
     */
    fun onRegenerateApprovalDietPlan(currentPlanId: String, feedback: String?) {
        viewModelScope.launch {
            _detailUiState.value = DietUiState.Regenerating

            when (val deleteResult = deleteDietPlanUseCase(currentPlanId)) {
                is Result.Error -> {
                    _detailUiState.value = DietUiState.Error(deleteResult.exception.toMessage())
                    emitEvent(DietUiEvent.ShowSnackbar(deleteResult.exception.toMessage()))
                    return@launch
                }
                else -> Unit
            }

            // Get user profile to build adaptive request
            when (val profileResult = getUserProfileUseCase().first { it !is Result.Loading }) {
                is Result.Success -> {
                    val profile = profileResult.data
                    val request = GenerateAdaptiveDietPlanRequestDto(
                        durationWeeks = 8,
                        mealsPerDay = 3,
                        dietPreference = profile.dietPreference?.name ?: "NONE",
                        goalType = profile.goalType?.name,
                        userConsiderations = feedback,
                        includeNutritionHistory = true,
                    )
                    when (val planResult = generateDietPlanUseCase.invokeAdaptive(request)) {
                        is Result.Success -> {
                            emitEvent(DietUiEvent.NavigateToDietApproval(planResult.data.id))
                        }
                        is Result.Error -> {
                            _detailUiState.value = DietUiState.Error(planResult.exception.toMessage())
                            emitEvent(DietUiEvent.ShowSnackbar(planResult.exception.toMessage()))
                        }
                        else -> Unit
                    }
                }
                is Result.Error -> {
                    _detailUiState.value = DietUiState.Error(profileResult.exception.toMessage())
                    emitEvent(DietUiEvent.ShowSnackbar(profileResult.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    /**
     * Generate a standard plan, update [generateUiState], and emit [DietUiEvent.NavigateToDetail].
     *
     * @param request Plan generation parameters.
     */
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

    /**
     * Generate an adaptive plan, update [generateUiState], and emit [DietUiEvent.NavigateToDetail].
     *
     * @param request Adaptive parameters of the plan.
     */
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

    /**
     * Reserved for navigation to generation; the screen delegates to navigation lambdas.
     *
     * @param adaptive true for adaptive flow.
     * @param basePlanId Optional base plan for regeneration.
     */
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
