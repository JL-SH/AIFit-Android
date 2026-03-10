package com.jlsh.aifit.feature.auth.ui.state

import com.jlsh.aifit.feature.auth.domain.model.AuthToken

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(val token: AuthToken) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

