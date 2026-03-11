package com.jlsh.aifit.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.jlsh.aifit.feature.auth.ui.LoginScreen
import com.jlsh.aifit.feature.auth.ui.RegisterScreen
import com.jlsh.aifit.feature.user.ui.CreateProfileScreen

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
            CreateProfileScreen(
                onNavigateToMain = {
                    navController.navigate(MainRoutes.GRAPH) {
                        popUpTo(AuthRoutes.GRAPH) { inclusive = true }
                    }
                },
            )
        }
    }
}

