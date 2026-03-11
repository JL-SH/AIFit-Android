package com.jlsh.aifit.feature.vision.ui.state

sealed class VisionUiEvent {
    data class NavigateToTrackMeal(val prefilled: String) : VisionUiEvent()
    data object NavigateBack : VisionUiEvent()
    data class ShowSnackbar(val message: String) : VisionUiEvent()
}

