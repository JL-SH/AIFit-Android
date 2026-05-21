package com.jlsh.aifit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.jlsh.aifit.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel raíz de navegación: destino inicial según sesión y logout forzado.
 *
 * **Estado / señales expuestas:**
 * - [startDestination]: grafo auth o main según login y perfil completo.
 * - [authStartDestination]: login o crear perfil dentro del grafo auth.
 * - [logoutNavigationEvent]: emisión única al cerrar sesión (p. ej. token expirado).
 * - [sessionExpiredMessage]: mensaje para mostrar en login tras logout forzado.
 *
 * @param sessionManager Gestión de sesión, perfil completo y eventos de logout.
 */
@HiltViewModel
class AppNavViewModel @Inject constructor(
    private val sessionManager: SessionManager,
) : ViewModel() {

    /** Destino inicial del [NavHost] raíz (`auth` o `main`). */
    val startDestination: String = when {
        !sessionManager.isLoggedIn.value -> AuthRoutes.GRAPH
        !sessionManager.isProfileComplete() -> AuthRoutes.GRAPH
        else -> MainRoutes.GRAPH
    }

    /** Destino inicial dentro del subgrafo de autenticación. */
    val authStartDestination: String = when {
        sessionManager.isLoggedIn.value && !sessionManager.isProfileComplete() ->
            AuthRoutes.CREATE_PROFILE
        else -> AuthRoutes.LOGIN
    }

    private val _logoutNavigationEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    /** Flujo que notifica que la app debe navegar a auth tras un logout forzado. */
    val logoutNavigationEvent = _logoutNavigationEvent.asSharedFlow()

    private val _sessionExpiredMessage = MutableStateFlow<String?>(null)

    /** Mensaje para la pantalla de login tras cierre de sesión (p. ej. token expirado). */
    val sessionExpiredMessage: StateFlow<String?> = _sessionExpiredMessage.asStateFlow()

    /** Limpia [sessionExpiredMessage] tras mostrarlo en login. */
    fun clearSessionExpiredMessage() {
        _sessionExpiredMessage.value = null
    }

    init {
        viewModelScope.launch {
            sessionManager.logoutEvent.collect { message ->
                _sessionExpiredMessage.value = message
                _logoutNavigationEvent.emit(Unit)
            }
        }
    }
}

/**
 * Grafo de navegación raíz: alterna entre flujo de autenticación y shell principal con pestañas.
 *
 * Escucha [AppNavViewModel.logoutNavigationEvent] para resetear la pila hacia auth y pasa
 * [AppNavViewModel.sessionExpiredMessage] al login.
 *
 * @param viewModel ViewModel de navegación raíz; por defecto Hilt.
 */
@Composable
fun AppNavGraph(viewModel: AppNavViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val sessionExpiredMessage by viewModel.sessionExpiredMessage.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.logoutNavigationEvent.collect {
            navController.navigate(AuthRoutes.GRAPH) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = viewModel.startDestination,
    ) {
        authNavGraph(
            navController = navController,
            startDestination = viewModel.authStartDestination,
            sessionExpiredMessage = sessionExpiredMessage,
            onSessionExpiredMessageShown = viewModel::clearSessionExpiredMessage,
        )
        mainNavGraph(navController = navController)
    }
}
