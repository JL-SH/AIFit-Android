package com.jlsh.aifit.feature.progression.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.feature.progression.domain.model.PlanProgressionSummary
import com.jlsh.aifit.feature.progression.domain.model.ProgressionRecommendation
import com.jlsh.aifit.feature.progression.domain.usecase.GetExerciseProgressionRecommendationUseCase
import com.jlsh.aifit.feature.progression.domain.usecase.GetFullPlanProgressionRecommendationsUseCase
import com.jlsh.aifit.feature.workout.domain.usecase.GetWorkoutHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Status of the progression recommendation for a specific exercise.
 */
sealed class RecommendationState {
    /** No active request (e.g. sheet closed).*/
    data object Idle : RecommendationState()

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
 * - [sessionCount]: Number of sessions completed (context for the UI).
 *
 * @param getExerciseRecommendationUseCase Individual recommendation.
 * @param getPlanRecommendationsUseCase Plan summary.
 * @param getWorkoutHistoryUseCase Session count for context.
 */
@HiltViewModel
class ProgressionViewModel @Inject constructor(
    private val getExerciseRecommendationUseCase: GetExerciseProgressionRecommendationUseCase,
    private val getPlanRecommendationsUseCase: GetFullPlanProgressionRecommendationsUseCase,
    private val getWorkoutHistoryUseCase: GetWorkoutHistoryUseCase,
) : ViewModel() {

    private val _recommendationState = MutableStateFlow<RecommendationState>(RecommendationState.Idle)
    /** Status of the recommendation of the selected exercise.*/
    val recommendationState: StateFlow<RecommendationState> = _recommendationState.asStateFlow()

    private val _planSummaryState = MutableStateFlow<PlanSummaryState>(PlanSummaryState.Idle)
    /** Plan Progression Summary Status.*/
    val planSummaryState: StateFlow<PlanSummaryState> = _planSummaryState.asStateFlow()

    private val _sessionCount = MutableStateFlow<Int?>(null)
    /** Number of training sessions recorded, or `null` while calculating.*/
    val sessionCount: StateFlow<Int?> = _sessionCount.asStateFlow()

    init {
        loadSessionCount()
    }

    private fun loadSessionCount() {
        viewModelScope.launch {
            when (val result = getWorkoutHistoryUseCase().first { it !is Result.Loading }) {
                is Result.Success -> _sessionCount.value = result.data.size
                is Result.Error -> _sessionCount.value = 0
                else -> Unit
            }
        }
    }

    /**
     * Load the progression recommendation for an exercise.
     *
     * @param exerciseId Exercise identifier.
     */
    fun loadExerciseRecommendation(exerciseId: String) {
        viewModelScope.launch {
            _recommendationState.value = RecommendationState.Loading
            when (val result = getExerciseRecommendationUseCase(exerciseId)) {
                is Result.Success -> _recommendationState.value = RecommendationState.Success(result.data)
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
        _recommendationState.value = RecommendationState.Idle
    }

    /** Restablece [planSummaryState] a [PlanSummaryState.Idle]. */
    fun resetPlanSummaryState() {
        _planSummaryState.value = PlanSummaryState.Idle
    }
}

