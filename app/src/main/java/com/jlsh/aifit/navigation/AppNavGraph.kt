package com.jlsh.aifit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.jlsh.aifit.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _logoutEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val logoutEvent = _logoutEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            sessionManager.logoutEvent.collect {
                _logoutEvent.emit(Unit)
            }
        }
    }
}

@Composable
fun AppNavGraph(viewModel: AppNavViewModel = hiltViewModel()) {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        viewModel.logoutEvent.collect {
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
        )
        mainNavGraph(navController = navController)
    }
}
