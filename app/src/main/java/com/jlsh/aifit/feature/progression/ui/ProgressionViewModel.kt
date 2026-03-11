package com.jlsh.aifit.feature.progression.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.feature.progression.domain.model.PlanProgressionSummary
import com.jlsh.aifit.feature.progression.domain.model.ProgressionRecommendation
import com.jlsh.aifit.feature.progression.domain.usecase.GetExerciseProgressionRecommendationUseCase
import com.jlsh.aifit.feature.progression.domain.usecase.GetFullPlanProgressionRecommendationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RecommendationState {
    data object Idle : RecommendationState()
    data object Loading : RecommendationState()
    data class Success(val data: ProgressionRecommendation) : RecommendationState()
    data class Error(val message: String) : RecommendationState()
}

sealed class PlanSummaryState {
    data object Idle : PlanSummaryState()
    data object Loading : PlanSummaryState()
    data class Success(val data: PlanProgressionSummary) : PlanSummaryState()
    data class Error(val message: String) : PlanSummaryState()
}

@HiltViewModel
class ProgressionViewModel @Inject constructor(
    private val getExerciseRecommendationUseCase: GetExerciseProgressionRecommendationUseCase,
    private val getPlanRecommendationsUseCase: GetFullPlanProgressionRecommendationsUseCase,
) : ViewModel() {

    private val _recommendationState = MutableStateFlow<RecommendationState>(RecommendationState.Idle)
    val recommendationState: StateFlow<RecommendationState> = _recommendationState.asStateFlow()

    private val _planSummaryState = MutableStateFlow<PlanSummaryState>(PlanSummaryState.Idle)
    val planSummaryState: StateFlow<PlanSummaryState> = _planSummaryState.asStateFlow()

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

    fun resetRecommendationState() {
        _recommendationState.value = RecommendationState.Idle
    }

    fun resetPlanSummaryState() {
        _planSummaryState.value = PlanSummaryState.Idle
    }
}

