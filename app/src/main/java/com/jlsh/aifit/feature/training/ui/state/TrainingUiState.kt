package com.jlsh.aifit.feature.training.ui.state

import com.jlsh.aifit.core.ui.components.layout.UiStateHost
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan

sealed class TrainingUiState {
    data object Loading : TrainingUiState(), UiStateHost.Loading
    data class Error(override val message: String) : TrainingUiState(), UiStateHost.Error
    data class Success(
        val plans: List<TrainingPlan> = emptyList(),
        val activePlan: TrainingPlan? = null,
        val selectedTabIndex: Int = 0,
        val isRefreshing: Boolean = false,
    ) : TrainingUiState(), UiStateHost.Success
}

sealed class TrainingDetailUiState {
    data object Loading : TrainingDetailUiState(), UiStateHost.Loading
    data class Error(override val message: String) : TrainingDetailUiState(), UiStateHost.Error
    data class Success(
        val plan: TrainingPlan,
    ) : TrainingDetailUiState(), UiStateHost.Success
}

sealed class GeneratePlanUiState {
    data object Idle : GeneratePlanUiState()
    data object Loading : GeneratePlanUiState()
    data class Error(val message: String) : GeneratePlanUiState()
    data class Success(val plan: TrainingPlan) : GeneratePlanUiState()
}

