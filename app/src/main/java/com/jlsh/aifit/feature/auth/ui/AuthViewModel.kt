package com.jlsh.aifit.feature.auth.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel compartido por login y registro.
 *
 * **UiState expuesto** ([uiState] — [AuthUiState]):
 * - [AuthUiState.Idle]: formulario listo, sin petición en curso.
 * - [AuthUiState.Loading]: login, registro o Google en progreso.
 * - [AuthUiState.Success]: autenticación correcta; incluye [com.jlsh.aifit.feature.auth.domain.model.AuthToken].
 * - [AuthUiState.Error]: mensaje de error (poco usado; los fallos suelen ir por eventos).
 *
 * **Eventos emitidos** ([events] — [AuthUiEvent]):
 * - [AuthUiEvent.NavigateToMain]: perfil completo → pantalla principal.
 * - [AuthUiEvent.NavigateToCreateProfile]: falta onboarding de perfil.
 * - [AuthUiEvent.NavigateToRegister]: ir al registro desde login.
 * - [AuthUiEvent.NavigateBack]: volver desde registro.
 * - [AuthUiEvent.ShowSnackbar]: mostrar mensaje de error al usuario.
 *
 * @param loginUseCase Autenticación con email/contraseña.
 * @param registerUseCase Alta de cuenta nueva.
 * @param googleLoginUseCase Autenticación con token de Google.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val googleLoginUseCase: GoogleLoginUseCase,
) : ViewModel() {

    // 1. UI STATE
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)

    /** Estado de la operación de autenticación (carga, éxito, error). */
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // 2. EVENTS CHANNEL
    private val _events = Channel<AuthUiEvent>(Channel.BUFFERED)

    /** Flujo único de navegación y snackbars; consumir una vez por pantalla. */
    val events = _events.receiveAsFlow()

    // 3. FORM STATE
    private val _email = MutableStateFlow("")

    /** Email introducido en el formulario. */
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")

    /** Contraseña introducida en el formulario. */
    val password: StateFlow<String> = _password.asStateFlow()

    private val _name = MutableStateFlow("")

    /** Nombre (solo pantalla de registro). */
    val name: StateFlow<String> = _name.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)

    /** Mensaje de validación del email, o `null` si es válido. */
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)

    /** Mensaje de validación de la contraseña, o `null` si es válida. */
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _nameError = MutableStateFlow<String?>(null)

    /** Mensaje de validación del nombre, o `null` si es válido. */
    val nameError: StateFlow<String?> = _nameError.asStateFlow()

    // 5. PUBLIC FUNCTIONS

    /**
     * Actualiza el email y limpia el error asociado.
     *
     * @param value Nuevo texto del campo.
     */
    fun onEmailChanged(value: String) {
        _email.value = value
        _emailError.value = null
    }

    /**
     * Actualiza la contraseña y limpia el error asociado.
     *
     * @param value Nuevo texto del campo.
     */
    fun onPasswordChanged(value: String) {
        _password.value = value
        _passwordError.value = null
    }

    /**
     * Actualiza el nombre y limpia el error asociado.
     *
     * @param value Nuevo texto del campo.
     */
    fun onNameChanged(value: String) {
        _name.value = value
        _nameError.value = null
    }

    /** Valida el formulario de login y lanza la petición si es correcto. */
    fun onLoginClicked() {
        if (!validateLoginFields()) return
        performLogin()
    }

    /** Valida el formulario de registro y lanza la petición si es correcto. */
    fun onRegisterClicked() {
        if (!validateRegisterFields()) return
        performRegister()
    }

    /**
     * Procesa el ID token devuelto por Google Sign-In.
     *
     * @param idToken Token JWT de Google.
     */
    fun onGoogleLoginResult(idToken: String) {
        performGoogleLogin(idToken)
    }

    /** Emite [AuthUiEvent.NavigateToRegister]. */
    fun onNavigateToRegister() {
        emitEvent(AuthUiEvent.NavigateToRegister)
    }

    /** Emite [AuthUiEvent.NavigateBack]. */
    fun onNavigateBack() {
        emitEvent(AuthUiEvent.NavigateBack)
    }

    // 6. PRIVATE HELPERS
    private fun validateLoginFields(): Boolean {
        var isValid = true

        if (_email.value.isBlank()) {
            _emailError.value = "El email es obligatorio"
            isValid = false
        } else if (!EMAIL_REGEX.matches(_email.value)) {
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
                    if (result.data.profileComplete) {
                        emitEvent(AuthUiEvent.NavigateToMain)
                    } else {
                        emitEvent(AuthUiEvent.NavigateToCreateProfile)
                    }
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
            Log.d("AIFIT", "AuthViewModel: performGoogleLogin — enviando token al backend")
            _uiState.value = AuthUiState.Loading
            when (val result = googleLoginUseCase(idToken)) {
                is Result.Success -> {
                    Log.d("AIFIT", "AuthViewModel: Google login exitoso — profileComplete=${result.data.profileComplete}")
                    _uiState.value = AuthUiState.Success(result.data)
                    if (result.data.profileComplete) {
                        emitEvent(AuthUiEvent.NavigateToMain)
                    } else {
                        emitEvent(AuthUiEvent.NavigateToCreateProfile)
                    }
                }
                is Result.Error -> {
                    Log.e("AIFIT", "AuthViewModel: Google login falló — ${result.exception}")
                    _uiState.value = AuthUiState.Idle
                    emitEvent(AuthUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun emitEvent(event: AuthUiEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    companion object {
        private val EMAIL_REGEX = Regex(
            "[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}"
        )
    }
}
