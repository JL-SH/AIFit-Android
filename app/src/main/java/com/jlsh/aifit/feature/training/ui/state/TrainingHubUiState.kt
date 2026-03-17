package com.jlsh.aifit.feature.training.ui.state

import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.training.domain.model.TrainingDay
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan

sealed interface TrainingHubUiState {

    data object Loading : TrainingHubUiState

    data class Error(val message: String) : TrainingHubUiState

    data object NoActivePlan : TrainingHubUiState

    data class ActivePlan(
        val plan: TrainingPlan,
        val currentWeek: Int,
        val nextDay: TrainingDay?,
        val allPlans: List<TrainingPlan>,
        val selectedFilter: PlanStatus?,
    ) : TrainingHubUiState
}

