package com.jlsh.aifit.feature.progress.ui.state

sealed class ProgressUiEvent {
    data object NavigateToBodyWeight : ProgressUiEvent()
    data object NavigateToWeeklySummary : ProgressUiEvent()
    data object NavigateToMetabolic : ProgressUiEvent()
    data object NavigateBack : ProgressUiEvent()
    data class ShowSnackbar(val message: String) : ProgressUiEvent()
    data object WeightLoggedSuccessfully : ProgressUiEvent()
}

