package com.jlsh.aifit.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
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
 * ViewModel shared by login and registration.
 *
 * **UiState exposed** ([uiState] — [AuthUiState]):
 * - [AuthUiState.Idle]: form ready, no request in progress.
 * - [AuthUiState.Loading]: login, registration or Google in progress.
 * - [AuthUiState.Success]: successful authentication; includes [com.jlsh.aifit.feature.auth.domain.model.AuthToken].
 * - [AuthUiState.Error]: error message (rarely used; failures are usually event-driven).
 *
 * **Emitted events** ([events] — [AuthUiEvent]):
 * - [AuthUiEvent.NavigateToMain]: Complete profile → main screen.
 * - [AuthUiEvent.NavigateToCreateProfile]: profile onboarding missing.
 * - [AuthUiEvent.NavigateToRegister]: go to the registry from login.
 * - [AuthUiEvent.NavigateBack]: return from registration.
 * - [AuthUiEvent.ShowSnackbar]: show error message to user.
 *
 * @param loginUseCase Authentication with email/password.
 * @param registerUseCase New account registration.
 * @param googleLoginUseCase Authentication with Google token.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val googleLoginUseCase: GoogleLoginUseCase,
) : ViewModel() {

    // 1. UI STATE
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)

    /** Status of the authentication operation (upload, success, error).*/
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // 2. EVENTS CHANNEL
    private val _events = Channel<AuthUiEvent>(Channel.BUFFERED)

    /** Unique flow of navigation and snackbars; consume once per screen.*/
    val events = _events.receiveAsFlow()

    // 3. FORM STATE
    private val _email = MutableStateFlow("")

    /** Email entered in the form.*/
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")

    /** Password entered in the form.*/
    val password: StateFlow<String> = _password.asStateFlow()

    private val _name = MutableStateFlow("")

    /** Name (registration screen only).*/
    val name: StateFlow<String> = _name.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)

    /** Email validation message, or `null` if valid.*/
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)

    /** Password validation message, or `null` if valid.*/
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _nameError = MutableStateFlow<String?>(null)

    /** Name validation message, or `null` if valid.*/
    val nameError: StateFlow<String?> = _nameError.asStateFlow()

    // 5. PUBLIC FUNCTIONS

    /**
     * Updates the email and clears the associated error.
     *
     * @param value New text for the field.
     */
    fun onEmailChanged(value: String) {
        _email.value = value
        _emailError.value = null
    }

    /**
     * Update the password and clear the associated error.
     *
     * @param value New text for the field.
     */
    fun onPasswordChanged(value: String) {
        _password.value = value
        _passwordError.value = null
    }

    /**
     * Updates the name and clears the associated error.
     *
     * @param value New text for the field.
     */
    fun onNameChanged(value: String) {
        _name.value = value
        _nameError.value = null
    }

    /** Validate the login form and launch the request if it is correct.*/
    fun onLoginClicked() {
        if (!validateLoginFields()) return
        performLogin()
    }

    /** Validate the registration form and launch the request if it is correct.*/
    fun onRegisterClicked() {
        if (!validateRegisterFields()) return
        performRegister()
    }

    /**
     * Processes the ID token returned by Google Sign-In.
     *
     * @param idToken Google JWT ID token.
     */
    fun onGoogleLoginResult(idToken: String) {
        performGoogleLogin(idToken)
    }

    /** Emite [AuthUiEvent.NavigateToRegister]. */
    fun onNavigateToRegister() {
        viewModelScope.launch { emitEvent(AuthUiEvent.NavigateToRegister) }
    }

    /** Emite [AuthUiEvent.NavigateBack]. */
    fun onNavigateBack() {
        viewModelScope.launch { emitEvent(AuthUiEvent.NavigateBack) }
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
                    emitEvent(AuthUiEvent.ShowSnackbar(result.exception.userMessage()))
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
                    emitEvent(AuthUiEvent.ShowSnackbar(result.exception.userMessage()))
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun performGoogleLogin(idToken: String) {
        viewModelScope.launch {
            safeLogDebug("AuthViewModel: performGoogleLogin — enviando token al backend")
            _uiState.value = AuthUiState.Loading
            when (val result = googleLoginUseCase(idToken)) {
                is Result.Success -> {
                    safeLogDebug("AuthViewModel: Google login exitoso — profileComplete=${result.data.profileComplete}")
                    _uiState.value = AuthUiState.Success(result.data)
                    if (result.data.profileComplete) {
                        emitEvent(AuthUiEvent.NavigateToMain)
                    } else {
                        emitEvent(AuthUiEvent.NavigateToCreateProfile)
                    }
                }
                is Result.Error -> {
                    safeLogError("AuthViewModel: Google login falló — ${result.exception}")
                    _uiState.value = AuthUiState.Idle
                    emitEvent(AuthUiEvent.ShowSnackbar(result.exception.userMessage()))
                }
                is Result.Loading -> Unit
            }
        }
    }

    private suspend fun emitEvent(event: AuthUiEvent) {
        _events.send(event)
    }

    private fun safeLogDebug(message: String) {
        runCatching { android.util.Log.d("AIFIT", message) }
    }

    private fun safeLogError(message: String) {
        runCatching { android.util.Log.e("AIFIT", message) }
    }

    private fun AppException.userMessage(): String = when (this) {
        is AppException.NetworkException -> "Sin conexión. Comprueba tu internet."
        is AppException.UnauthorizedException -> "Sesión expirada. Vuelve a iniciar sesión."
        is AppException.ForbiddenException -> "No tienes permisos para realizar esta acción."
        is AppException.NotFoundException -> "No se encontró $resource."
        is AppException.ValidationException -> errors.values.firstOrNull() ?: "Datos inválidos."
        is AppException.ConflictException -> "El recurso ya existe o hay un conflicto."
        is AppException.ServerException -> "Error del servidor. Inténtalo más tarde."
        is AppException.AiOverloadedException -> AppException.AI_OVERLOADED_MESSAGE
        is AppException.UnknownException -> message.ifBlank { "Error inesperado. Inténtalo de nuevo." }
        is AppException.InsufficientDataException -> "Necesitas más datos para realizar este análisis. Registra al menos 2 semanas de peso y entrenamientos."
    }

    companion object {
        private val EMAIL_REGEX = Regex(
            "[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}"
        )
    }
}
