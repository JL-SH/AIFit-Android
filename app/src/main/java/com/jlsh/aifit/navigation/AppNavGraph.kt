package com.jlsh.aifit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.jlsh.aifit.core.datastore.UserPreferencesDataStore
import com.jlsh.aifit.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppNavViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val userPreferencesDataStore: UserPreferencesDataStore,
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean> = sessionManager.isLoggedIn

    val hasCompletedOnboarding: StateFlow<Boolean> = userPreferencesDataStore.hasCompletedOnboarding
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

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
fun AppNavGraph(
    viewModel: AppNavViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()

    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsState()

    val startDestination = when {
        !isLoggedIn -> AuthRoutes.GRAPH
        !hasCompletedOnboarding -> AuthRoutes.GRAPH
        else -> MainRoutes.GRAPH
    }

    LaunchedEffect(Unit) {
        viewModel.logoutEvent.collect {
            navController.navigate(AuthRoutes.GRAPH) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        authNavGraph(
            navController = navController,
            hasCompletedOnboarding = hasCompletedOnboarding,
        )
        mainNavGraph(navController = navController)
    }
}
