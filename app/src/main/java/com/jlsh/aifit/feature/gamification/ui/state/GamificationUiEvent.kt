package com.jlsh.aifit.feature.gamification.ui.state

sealed class GamificationUiEvent {
    data object NavigateToExport : GamificationUiEvent()
    data object NavigateBack : GamificationUiEvent()
    data class ShowSnackbar(val message: String) : GamificationUiEvent()
}

