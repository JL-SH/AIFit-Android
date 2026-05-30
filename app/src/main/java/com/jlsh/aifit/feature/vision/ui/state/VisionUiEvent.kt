package com.jlsh.aifit.feature.vision.ui.state

sealed class VisionUiEvent {
    data object NavigateBack : VisionUiEvent()
    data class ShowSnackbar(val message: String) : VisionUiEvent()
    data object MealLogged : VisionUiEvent()
}

