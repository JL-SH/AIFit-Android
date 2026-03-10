package com.jlsh.aifit.feature.user.ui.state

sealed class UserUiEvent {
    data object NavigateToEditProfile : UserUiEvent()
    data object NavigateBack : UserUiEvent()
    data object ProfileSaved : UserUiEvent()
    data class ShowSnackbar(val message: String) : UserUiEvent()
    data object Logout : UserUiEvent()
}

