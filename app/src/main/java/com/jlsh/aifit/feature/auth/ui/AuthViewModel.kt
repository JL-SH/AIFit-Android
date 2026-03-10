package com.jlsh.aifit.feature.auth.ui

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.core.datastore.UserPreferencesDataStore
import com.jlsh.aifit.feature.auth.domain.usecase.GoogleLoginUseCase
import com.jlsh.aifit.feature.auth.domain.usecase.LoginUseCase
import com.jlsh.aifit.feature.auth.domain.usecase.RegisterUseCase
import com.jlsh.aifit.feature.auth.ui.state.AuthUiEvent
import com.jlsh.aifit.feature.auth.ui.state.AuthUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val googleLoginUseCase: GoogleLoginUseCase,
    private val userPreferencesDataStore: UserPreferencesDataStore,
) : ViewModel() {

    // 1. UI STATE
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // 2. EVENTS CHANNEL
    private val _events = Channel<AuthUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // 3. FORM STATE
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()

    // 5. PUBLIC FUNCTIONS
    fun onEmailChanged(value: String) {
        _email.value = value
        _emailError.value = null
    }

    fun onPasswordChanged(value: String) {
        _password.value = value
        _passwordError.value = null
    }

    fun onNameChanged(value: String) {
        _name.value = value
        _nameError.value = null
    }

    fun onLoginClicked() {
        if (!validateLoginFields()) return
        performLogin()
    }

    fun onRegisterClicked() {
        if (!validateRegisterFields()) return
        performRegister()
    }

    fun onGoogleLoginResult(idToken: String) {
        performGoogleLogin(idToken)
    }

    fun onNavigateToRegister() {
        emitEvent(AuthUiEvent.NavigateToRegister)
    }

    fun onNavigateBack() {
        emitEvent(AuthUiEvent.NavigateBack)
    }

    // 6. PRIVATE HELPERS
    private fun validateLoginFields(): Boolean {
        var isValid = true

        if (_email.value.isBlank()) {
            _emailError.value = "El email es obligatorio"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(_email.value).matches()) {
            _emailError.value = "Introduce un email válido"
            isValid = false
        }

        if (_password.value.isBlank()) {
            _passwordError.value = "La contraseña es obligatoria"
            isValid = false
        } else if (_password.value.length < 6) {
            _passwordError.value = "Mínimo 6 caracteres"
            isValid = false
        }

        return isValid
    }

    private fun validateRegisterFields(): Boolean {
        var isValid = validateLoginFields()

        if (_name.value.isBlank()) {
            _nameError.value = "El nombre es obligatorio"
            isValid = false
        }

        return isValid
    }

    private fun performLogin() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = loginUseCase(_email.value.trim(), _password.value)) {
                is Result.Success -> {
                    _uiState.value = AuthUiState.Success(result.data)
                    navigateAfterAuth()
                }
                is Result.Error -> {
                    _uiState.value = AuthUiState.Idle
                    emitEvent(AuthUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun performRegister() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = registerUseCase(_email.value.trim(), _password.value, _name.value.trim())) {
                is Result.Success -> {
                    _uiState.value = AuthUiState.Success(result.data)
                    emitEvent(AuthUiEvent.NavigateToCreateProfile)
                }
                is Result.Error -> {
                    _uiState.value = AuthUiState.Idle
                    emitEvent(AuthUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun performGoogleLogin(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = googleLoginUseCase(idToken)) {
                is Result.Success -> {
                    _uiState.value = AuthUiState.Success(result.data)
                    navigateAfterAuth()
                }
                is Result.Error -> {
                    _uiState.value = AuthUiState.Idle
                    emitEvent(AuthUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                is Result.Loading -> Unit
            }
        }
    }

    private suspend fun navigateAfterAuth() {
        val hasOnboarding = userPreferencesDataStore.hasCompletedOnboarding.first()
        if (hasOnboarding) {
            emitEvent(AuthUiEvent.NavigateToMain)
        } else {
            emitEvent(AuthUiEvent.NavigateToCreateProfile)
        }
    }

    private fun emitEvent(event: AuthUiEvent) {
        viewModelScope.launch { _events.send(event) }
    }
}

