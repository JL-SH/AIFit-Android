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
import com.jlsh.aifit.feature.user.ui.OnboardingGeneratingScreen
import com.jlsh.aifit.feature.user.ui.OnboardingNutritionApprovalScreen
import com.jlsh.aifit.feature.user.ui.OnboardingTrainingApprovalScreen
import com.jlsh.aifit.feature.user.ui.OnboardingViewModel

/**
 * Registers the authentication subgraph: login, registration, profile and onboarding.
 *
 * @param navController Controller for the root [NavHost].
 * @param startDestination Initial path within the graph (`login` or `create_profile`).
 * @param sessionExpiredMessage Expired session message to display at login, or null.
 * @param onSessionExpiredMessageShown Callback when consuming the message in login.
 */
fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    startDestination: String = AuthRoutes.LOGIN,
    sessionExpiredMessage: String? = null,
    onSessionExpiredMessageShown: () -> Unit = {},
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
                sessionExpiredMessage = sessionExpiredMessage,
                onSessionExpiredMessageShown = onSessionExpiredMessageShown,
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
                    navController.navigate(AuthRoutes.ONBOARDING_TRAINING_APPROVAL) {
                        popUpTo(AuthRoutes.ONBOARDING_GENERATING) { inclusive = true }
                    }
                },
                viewModel = onboardingViewModel,
            )
        }

        composable(AuthRoutes.ONBOARDING_TRAINING_APPROVAL) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(AuthRoutes.GRAPH)
            }
            val onboardingViewModel: OnboardingViewModel = hiltViewModel(parentEntry)
            OnboardingTrainingApprovalScreen(
                onApprove = {
                    navController.navigate(AuthRoutes.ONBOARDING_NUTRITION_APPROVAL) {
                        popUpTo(AuthRoutes.ONBOARDING_TRAINING_APPROVAL) { inclusive = false }
                    }
                },
                onRegenerate = {},
                viewModel = onboardingViewModel,
            )
        }

        composable(AuthRoutes.ONBOARDING_NUTRITION_APPROVAL) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(AuthRoutes.GRAPH)
            }
            val onboardingViewModel: OnboardingViewModel = hiltViewModel(parentEntry)
            OnboardingNutritionApprovalScreen(
                onApprove = {
                    navController.navigate(MainRoutes.GRAPH) {
                        popUpTo(AuthRoutes.GRAPH) { inclusive = true }
                    }
                },
                onRegenerate = {},
                viewModel = onboardingViewModel,
            )
        }
    }
}
