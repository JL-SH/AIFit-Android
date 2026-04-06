package com.jlsh.aifit.feature.training.ui.state

sealed class TrainingUiEvent {
    data class NavigateToDetail(val planId: String) : TrainingUiEvent()
    data class NavigateToGenerate(
        val adaptive: Boolean = false,
        val basePlanId: String? = null,
    ) : TrainingUiEvent()
    data class NavigateToApproval(val planId: String) : TrainingUiEvent()
    data class NavigateToWorkoutLog(val planId: String) : TrainingUiEvent()
    data object NavigateBack : TrainingUiEvent()
    data class ShowSnackbar(val message: String) : TrainingUiEvent()
    data object PlanDeleted : TrainingUiEvent()
}

