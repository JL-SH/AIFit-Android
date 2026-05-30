package com.jlsh.aifit.feature.progression.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.feature.progression.domain.ProgressionRequirements.MIN_SESSIONS_REQUIRED
import com.jlsh.aifit.feature.progression.domain.model.PlanProgressionSummary
import com.jlsh.aifit.feature.progression.domain.model.ProgressionRecommendation
import com.jlsh.aifit.feature.progression.domain.model.ProgressionType
import com.jlsh.aifit.feature.progression.domain.usecase.GetExerciseProgressionRecommendationUseCase
import com.jlsh.aifit.feature.progression.domain.usecase.GetFullPlanProgressionRecommendationsUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.GetExerciseLoggedSessionCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Status of the progression recommendation for a specific exercise.
 */
sealed class RecommendationState {
    /** No active request (e.g. sheet closed).*/
    data object Idle : RecommendationState()

    /** User may confirm generation after the intro copy (enough sessions recorded).*/
    data object PromptConfirm : RecommendationState()

    /**
     * Not enough workout history to request a recommendation.
     *
     * @property requiredSessions Minimum sessions required.
     * @property currentSessions Sessions the user has logged so far.
     */
    data class InsufficientData(
        val requiredSessions: Int,
        val currentSessions: Int,
    ) : RecommendationState()

    /** Loading of exercise recommendation.*/
    data object Loading : RecommendationState()

    /**
     * Recommendation available.
     *
     * @property data Suggested load, repetitions and justification.
     */
    data class Success(val data: ProgressionRecommendation) : RecommendationState()

    /**
     * Error loading recommendation.
     *
     * @property message Text to display to the user.
     */
    data class Error(val message: String) : RecommendationState()
}

/**
 * Progression summary status of a completed plan.
 */
sealed class PlanSummaryState {
    /** No active request.*/
    data object Idle : PlanSummaryState()

    /** Plan summary upload.*/
    data object Loading : PlanSummaryState()

    /**
     * Plan summary available.
     *
     * @property data Recommendations grouped by exercise.
     */
    data class Success(val data: PlanProgressionSummary) : PlanSummaryState()

    /**
     * Error loading summary.
     *
     * @property message Text to display to the user.
     */
    data class Error(val message: String) : PlanSummaryState()
}

/**
 * ViewModel of progression recommendations by exercise and by plan.
 *
 * **UiState exposed** (no event channel; UI observes StateFlows):
 * - [recommendationState] — [RecommendationState]: recommendation of an exercise.
 * - [planSummaryState] — [PlanSummaryState]: summary of the entire plan.
 * - [exerciseSessionCounts]: Logged sessions per exercise id (for list affordances).
 *
 * @param getExerciseRecommendationUseCase Individual recommendation.
 * @param getPlanRecommendationsUseCase Plan summary.
 * @param getExerciseLoggedSessionCountUseCase Per-exercise session count from workout history.
 */
@HiltViewModel
class ProgressionViewModel @Inject constructor(
    private val getExerciseRecommendationUseCase: GetExerciseProgressionRecommendationUseCase,
    private val getPlanRecommendationsUseCase: GetFullPlanProgressionRecommendationsUseCase,
    private val getExerciseLoggedSessionCountUseCase: GetExerciseLoggedSessionCountUseCase,
) : ViewModel() {

    private val _recommendationState = MutableStateFlow<RecommendationState>(RecommendationState.Idle)
    /** Status of the recommendation of the selected exercise.*/
    val recommendationState: StateFlow<RecommendationState> = _recommendationState.asStateFlow()

    private val _planSummaryState = MutableStateFlow<PlanSummaryState>(PlanSummaryState.Idle)
    /** Plan Progression Summary Status.*/
    val planSummaryState: StateFlow<PlanSummaryState> = _planSummaryState.asStateFlow()

    private val _exerciseSessionCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    /** Distinct logged sessions per training exercise id.*/
    val exerciseSessionCounts: StateFlow<Map<String, Int>> = _exerciseSessionCounts.asStateFlow()

    private var pendingExerciseId: String? = null

    /**
     * Opens the progression sheet for [exerciseId].
     * Resolves how many sessions include logged sets for this exercise before showing confirm or insufficient UI.
     */
    fun openExerciseProgression(exerciseId: String) {
        pendingExerciseId = exerciseId
        viewModelScope.launch {
            _recommendationState.value = RecommendationState.Loading
            val count = resolveExerciseSessionCount(exerciseId)
            cacheExerciseSessionCount(exerciseId, count)
            _recommendationState.value = recommendationStateForSessionCount(count)
        }
    }

    /** Requests the AI recommendation after the user confirms in [RecommendationState.PromptConfirm]. */
    fun confirmExerciseProgression() {
        val exerciseId = pendingExerciseId ?: return
        viewModelScope.launch {
            val count = resolveExerciseSessionCount(exerciseId)
            cacheExerciseSessionCount(exerciseId, count)
            if (count < MIN_SESSIONS_REQUIRED) {
                _recommendationState.value = RecommendationState.InsufficientData(
                    requiredSessions = MIN_SESSIONS_REQUIRED,
                    currentSessions = count,
                )
                return@launch
            }
            loadExerciseRecommendation(exerciseId)
        }
    }

    /**
     * Load the progression recommendation for an exercise.
     *
     * @param exerciseId Exercise identifier.
     */
    fun loadExerciseRecommendation(exerciseId: String) {
        pendingExerciseId = exerciseId
        viewModelScope.launch {
            _recommendationState.value = RecommendationState.Loading
            when (val result = getExerciseRecommendationUseCase(exerciseId)) {
                is Result.Success -> {
                    val data = result.data
                    cacheExerciseSessionCount(exerciseId, data.basedOnSessions)
                    _recommendationState.value = if (data.type == ProgressionType.INSUFFICIENT_DATA) {
                        RecommendationState.InsufficientData(
                            requiredSessions = MIN_SESSIONS_REQUIRED,
                            currentSessions = data.basedOnSessions,
                        )
                    } else {
                        RecommendationState.Success(data)
                    }
                }
                is Result.Error -> _recommendationState.value = RecommendationState.Error(result.exception.toMessage())
                else -> Unit
            }
        }
    }

    /**
     * Load the progression summary of the entire plan.
     *
     * @param planId Identifier of the training plan.
     */
    fun loadPlanRecommendations(planId: String) {
        viewModelScope.launch {
            _planSummaryState.value = PlanSummaryState.Loading
            when (val result = getPlanRecommendationsUseCase(planId)) {
                is Result.Success -> _planSummaryState.value = PlanSummaryState.Success(result.data)
                is Result.Error -> _planSummaryState.value = PlanSummaryState.Error(result.exception.toMessage())
                else -> Unit
            }
        }
    }

    /** Restablece [recommendationState] a [RecommendationState.Idle] (e.g. al cerrar el sheet). */
    fun resetRecommendationState() {
        pendingExerciseId = null
        _recommendationState.value = RecommendationState.Idle
    }

    /** Restablece [planSummaryState] a [PlanSummaryState.Idle]. */
    fun resetPlanSummaryState() {
        _planSummaryState.value = PlanSummaryState.Idle
    }

    private suspend fun resolveExerciseSessionCount(exerciseId: String): Int =
        when (val result = getExerciseLoggedSessionCountUseCase(exerciseId)) {
            is Result.Success -> result.data
            else -> 0
        }

    private fun cacheExerciseSessionCount(exerciseId: String, count: Int) {
        _exerciseSessionCounts.update { current -> current + (exerciseId to count) }
    }

    private fun recommendationStateForSessionCount(count: Int): RecommendationState =
        if (count >= MIN_SESSIONS_REQUIRED) {
            RecommendationState.PromptConfirm
        } else {
            RecommendationState.InsufficientData(
                requiredSessions = MIN_SESSIONS_REQUIRED,
                currentSessions = count,
            )
        }
}
