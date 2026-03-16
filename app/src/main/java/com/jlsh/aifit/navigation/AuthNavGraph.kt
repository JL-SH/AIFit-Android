package com.jlsh.aifit.navigation

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.jlsh.aifit.feature.auth.ui.LoginScreen
import com.jlsh.aifit.feature.auth.ui.RegisterScreen
import com.jlsh.aifit.feature.user.ui.CreateProfileScreen
import com.jlsh.aifit.feature.user.ui.OnboardingApprovalScreen
import com.jlsh.aifit.feature.user.ui.OnboardingGeneratingScreen
import com.jlsh.aifit.feature.user.ui.OnboardingViewModel

fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    startDestination: String = AuthRoutes.LOGIN,
) {
    navigation(
        route = AuthRoutes.GRAPH,
        startDestination = startDestination,
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
                onNavigateToOnboarding = {
                    navController.navigate(AuthRoutes.ONBOARDING_GENERATING) {
                        popUpTo(AuthRoutes.CREATE_PROFILE) { inclusive = true }
                    }
                },
            )
        }

        composable(AuthRoutes.ONBOARDING_GENERATING) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(AuthRoutes.GRAPH)
            }
            val onboardingViewModel: OnboardingViewModel = hiltViewModel(parentEntry)
            OnboardingGeneratingScreen(
                onSuccess = {
                    navController.navigate(AuthRoutes.ONBOARDING_APPROVAL) {
                        popUpTo(AuthRoutes.ONBOARDING_GENERATING) { inclusive = true }
                    }
                },
                viewModel = onboardingViewModel,
            )
        }

        composable(AuthRoutes.ONBOARDING_APPROVAL) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(AuthRoutes.GRAPH)
            }
            val onboardingViewModel: OnboardingViewModel = hiltViewModel(parentEntry)
            OnboardingApprovalScreen(
                onApprove = {
                    navController.navigate(MainRoutes.GRAPH) {
                        popUpTo(AuthRoutes.GRAPH) { inclusive = true }
                    }
                },
                onRegenerate = {
                    navController.navigate(AuthRoutes.ONBOARDING_GENERATING) {
                        popUpTo(AuthRoutes.ONBOARDING_APPROVAL) { inclusive = true }
                    }
                },
                viewModel = onboardingViewModel,
            )
        }
    }
}

