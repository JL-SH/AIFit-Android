package com.jlsh.aifit.feature.user.ui.state

import com.jlsh.aifit.core.ui.components.layout.UiStateHost
import com.jlsh.aifit.feature.user.domain.model.UserProfile

sealed class UserUiState {
    data object Idle : UserUiState()
    data object Loading : UserUiState(), UiStateHost.Loading
    data class Success(val profile: UserProfile) : UserUiState(), UiStateHost.Success
    data class Error(override val message: String) : UserUiState(), UiStateHost.Error
    data object Saving : UserUiState()
}

