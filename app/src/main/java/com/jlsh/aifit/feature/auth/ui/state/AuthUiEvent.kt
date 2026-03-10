package com.jlsh.aifit.feature.auth.ui.state

sealed class AuthUiEvent {
    data object NavigateToMain : AuthUiEvent()
    data object NavigateToCreateProfile : AuthUiEvent()
    data object NavigateToRegister : AuthUiEvent()
    data object NavigateBack : AuthUiEvent()
    data class ShowSnackbar(val message: String) : AuthUiEvent()
}

