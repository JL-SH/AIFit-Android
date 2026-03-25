package com.jlsh.aifit.feature.diet.ui.state

sealed class DietUiEvent {
    data class NavigateToDetail(val planId: String) : DietUiEvent()
    data object NavigateBack : DietUiEvent()
    data object NavigateToDietGenerate : DietUiEvent()
    data class ShowSnackbar(val message: String) : DietUiEvent()
}

