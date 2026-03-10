package com.jlsh.aifit.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.jlsh.aifit.feature.auth.ui.LoginScreen
import com.jlsh.aifit.feature.auth.ui.RegisterScreen

fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    hasCompletedOnboarding: Boolean,
) {
    navigation(
        route = AuthRoutes.GRAPH,
        startDestination = AuthRoutes.LOGIN,
    ) {
        composable(AuthRoutes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(AuthRoutes.REGISTER)
                },
                onNavigateToMain = {
                    navController.navigate(MainRoutes.GRAPH) {
                        popUpTo(AuthRoutes.GRAPH) { inclusive = true }
                    }
                },
                onNavigateToCreateProfile = {
                    navController.navigate(AuthRoutes.CREATE_PROFILE)
                },
            )
        }

        composable(AuthRoutes.REGISTER) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCreateProfile = {
                    navController.navigate(AuthRoutes.CREATE_PROFILE) {
                        popUpTo(AuthRoutes.LOGIN) { inclusive = false }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(MainRoutes.GRAPH) {
                        popUpTo(AuthRoutes.GRAPH) { inclusive = true }
                    }
                },
            )
        }

        composable(AuthRoutes.CREATE_PROFILE) {
            // Stub — Sprint 4
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Create Profile — Sprint 4",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

