package com.jlsh.aifit.feature.home.ui.state

sealed class HomeUiEvent {
    data class NavigateToWorkoutLog(val planId: String) : HomeUiEvent()
    data class NavigateToTrainingDetail(val planId: String) : HomeUiEvent()
    data object NavigateToTrackMeal : HomeUiEvent()
    data object NavigateToProgressDashboard : HomeUiEvent()
    data object NavigateToBodyWeight : HomeUiEvent()
    data class NavigateToGamification(val tab: String) : HomeUiEvent()
    data object NavigateToProfile : HomeUiEvent()
    data object NavigateToGeneratePlan : HomeUiEvent()
    data object ShowLogWeightSheet : HomeUiEvent()
    data class ShowSnackbar(val message: String) : HomeUiEvent()
}

