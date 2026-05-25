package com.jlsh.aifit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.jlsh.aifit.core.ui.theme.AiFitMotion
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
 * Navigation root ViewModel: initial destination based on session and forced logout.
 *
 * **State/exposed signs:**
 * - [startDestination]: auth or main graph depending on login and complete profile.
 * - [authStartDestination]: login or create profile within the auth graph.
 * - [logoutNavigationEvent]: Single issue on logout (e.g. token expired).
 * - [sessionExpiredMessage]: message to display at login after forced logout.
 *
 * @param sessionManager Session management, full profile and logout events.
 */
@HiltViewModel
class AppNavViewModel @Inject constructor(
    private val sessionManager: SessionManager,
) : ViewModel() {

    /** Initial destination of the root [NavHost] (`auth` or `main`).*/
    val startDestination: String = when {
        !sessionManager.isLoggedIn.value -> AuthRoutes.GRAPH
        !sessionManager.isProfileComplete() -> AuthRoutes.GRAPH
        else -> MainRoutes.GRAPH
    }

    /** Initial destination within the authentication subgraph.*/
    val authStartDestination: String = when {
        sessionManager.isLoggedIn.value && !sessionManager.isProfileComplete() ->
            AuthRoutes.CREATE_PROFILE
        else -> AuthRoutes.LOGIN
    }

    private val _logoutNavigationEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    /** Flow that notifies that the app must navigate to auth after a forced logout.*/
    val logoutNavigationEvent = _logoutNavigationEvent.asSharedFlow()

    private val _sessionExpiredMessage = MutableStateFlow<String?>(null)

    /** Message for the login screen after session closure (e.g. token expired).*/
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
 * Root navigation graph: Toggles between authentication flow and main shell with tabs.
 *
 * Listens to [AppNavViewModel.logoutNavigationEvent] to reset the stack to auth and passes
 * [AppNavViewModel.sessionExpiredMessage] to login.
 *
 * @param viewModel root navigation ViewModel; default Hilt.
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
        enterTransition = { fadeIn(AiFitMotion.standardTween<Float>()) },
        exitTransition = { fadeOut(AiFitMotion.standardTween<Float>()) },
        popEnterTransition = { fadeIn(AiFitMotion.standardTween<Float>()) },
        popExitTransition = { fadeOut(AiFitMotion.standardTween<Float>()) },
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
