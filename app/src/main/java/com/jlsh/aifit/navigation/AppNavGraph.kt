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

@HiltViewModel
class AppNavViewModel @Inject constructor(
    private val sessionManager: SessionManager,
) : ViewModel() {

    val startDestination: String = when {
        !sessionManager.isLoggedIn.value -> AuthRoutes.GRAPH
        !sessionManager.isProfileComplete() -> AuthRoutes.GRAPH
        else -> MainRoutes.GRAPH
    }

    val authStartDestination: String = when {
        sessionManager.isLoggedIn.value && !sessionManager.isProfileComplete() ->
            AuthRoutes.CREATE_PROFILE
        else -> AuthRoutes.LOGIN
    }

    private val _logoutNavigationEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val logoutNavigationEvent = _logoutNavigationEvent.asSharedFlow()

    /** Message shown on the Login screen after a forced logout (e.g. token expired). */
    private val _sessionExpiredMessage = MutableStateFlow<String?>(null)
    val sessionExpiredMessage: StateFlow<String?> = _sessionExpiredMessage.asStateFlow()

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
