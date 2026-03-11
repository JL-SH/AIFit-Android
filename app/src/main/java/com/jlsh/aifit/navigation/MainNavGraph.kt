package com.jlsh.aifit.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.jlsh.aifit.core.ui.components.layout.BottomNavBar
import com.jlsh.aifit.core.ui.components.layout.LocalBottomBarVisibility
import com.jlsh.aifit.core.ui.components.layout.bottomNavItems
import com.jlsh.aifit.feature.training.ui.GeneratePlanScreen
import com.jlsh.aifit.feature.training.ui.TrainingDetailScreen
import com.jlsh.aifit.feature.training.ui.TrainingHubScreen

private val tabRouteToGraphRoute = mapOf(
    "home" to HomeRoutes.GRAPH,
    "training" to TrainingRoutes.GRAPH,
    "nutrition" to NutritionRoutes.GRAPH,
    "coach" to CoachRoutes.GRAPH,
    "profile" to ProfileRoutes.GRAPH,
)

fun NavGraphBuilder.mainNavGraph(navController: NavController) {
    composable(MainRoutes.GRAPH) {
        MainNavScreen()
    }
}

@Composable
private fun MainNavScreen() {
    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine which tab graph is active based on the bottom nav item routes
    val currentTabRoute = bottomNavItems.firstOrNull { item ->
        currentRoute?.startsWith(item.route) == true
    }?.route

    CompositionLocalProvider(LocalBottomBarVisibility provides true) {
        val isBottomBarVisible = LocalBottomBarVisibility.current

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                AnimatedVisibility(
                    visible = isBottomBarVisible,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                ) {
                    BottomNavBar(
                        currentRoute = currentTabRoute,
                        onItemSelected = { item ->
                            val graphRoute = tabRouteToGraphRoute[item.route] ?: item.route
                            tabNavController.navigate(graphRoute) {
                                popUpTo(tabNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            },
        ) { paddingValues ->
            NavHost(
                navController = tabNavController,
                startDestination = HomeRoutes.GRAPH,
                modifier = Modifier.padding(paddingValues),
            ) {
                // Home tab
                navigation(
                    route = HomeRoutes.GRAPH,
                    startDestination = HomeRoutes.HOME,
                ) {
                    composable(HomeRoutes.HOME) {
                        StubScreen("Home — Sprint 5")
                    }
                    composable(HomeRoutes.DASHBOARD) {
                        StubScreen("Dashboard — Sprint 8")
                    }
                    composable(HomeRoutes.BODY_WEIGHT) {
                        StubScreen("Body Weight — Sprint 8")
                    }
                    composable(HomeRoutes.WEEKLY_SUMMARY) {
                        StubScreen("Weekly Summary — Sprint 8")
                    }
                    composable(HomeRoutes.METABOLIC_ANALYSIS) {
                        StubScreen("Metabolic Analysis — Sprint 8")
                    }
                }

                // Training tab
                navigation(
                    route = TrainingRoutes.GRAPH,
                    startDestination = TrainingRoutes.HUB,
                ) {
                    composable(TrainingRoutes.HUB) {
                        TrainingHubScreen(
                            onNavigateToDetail = { planId ->
                                tabNavController.navigate(TrainingRoutes.detailRoute(planId))
                            },
                            onNavigateToGenerate = { adaptive, basePlanId ->
                                tabNavController.navigate(TrainingRoutes.generateRoute(adaptive, basePlanId))
                            },
                            onNavigateToWorkoutLog = { planId ->
                                tabNavController.navigate(TrainingRoutes.workoutLogRoute(planId))
                            },
                        )
                    }
                    composable(
                        route = TrainingRoutes.DETAIL,
                        arguments = listOf(
                            navArgument("planId") { type = NavType.StringType },
                        ),
                    ) { backStackEntry ->
                        val planId = backStackEntry.arguments?.getString("planId") ?: ""
                        TrainingDetailScreen(
                            planId = planId,
                            onNavigateBack = { tabNavController.popBackStack() },
                            onNavigateToGenerate = { adaptive, basePlanId ->
                                tabNavController.navigate(TrainingRoutes.generateRoute(adaptive, basePlanId))
                            },
                            onNavigateToWorkoutLog = { pId ->
                                tabNavController.navigate(TrainingRoutes.workoutLogRoute(pId))
                            },
                        )
                    }
                    composable(
                        route = TrainingRoutes.GENERATE,
                        arguments = listOf(
                            navArgument("adaptive") {
                                type = NavType.StringType
                                defaultValue = "false"
                            },
                            navArgument("basePlanId") {
                                type = NavType.StringType
                                defaultValue = ""
                            },
                        ),
                    ) { backStackEntry ->
                        val adaptive = backStackEntry.arguments?.getString("adaptive")?.toBooleanStrictOrNull() ?: false
                        val basePlanId = backStackEntry.arguments?.getString("basePlanId")?.ifBlank { null }
                        GeneratePlanScreen(
                            adaptive = adaptive,
                            basePlanId = basePlanId,
                            onNavigateBack = { tabNavController.popBackStack() },
                            onNavigateToDetail = { newPlanId ->
                                tabNavController.navigate(TrainingRoutes.detailRoute(newPlanId)) {
                                    popUpTo(TrainingRoutes.GENERATE) { inclusive = true }
                                }
                            },
                        )
                    }
                    composable(TrainingRoutes.WORKOUT_LOG) {
                        StubScreen("Workout Log — Sprint 7")
                    }
                    composable(TrainingRoutes.WORKOUT_DETAIL) {
                        StubScreen("Workout Detail — Sprint 7")
                    }
                }

                // Nutrition tab
                navigation(
                    route = NutritionRoutes.GRAPH,
                    startDestination = NutritionRoutes.HUB,
                ) {
                    composable(NutritionRoutes.HUB) {
                        StubScreen("Nutrition Hub — Sprint 9")
                    }
                    composable(NutritionRoutes.TRACK_MEAL) {
                        StubScreen("Track Meal — Sprint 10")
                    }
                    composable(NutritionRoutes.FOOD_VISION) {
                        StubScreen("Food Vision — Sprint 12")
                    }
                    composable(NutritionRoutes.TARGET) {
                        StubScreen("Nutrition Target — Sprint 10")
                    }
                    composable(NutritionRoutes.DIET_DETAIL) {
                        StubScreen("Diet Detail — Sprint 9")
                    }
                    composable(NutritionRoutes.DIET_GENERATE) {
                        StubScreen("Generate Diet — Sprint 10")
                    }
                    composable(NutritionRoutes.SHOPPING_DETAIL) {
                        StubScreen("Shopping Detail — Sprint 11")
                    }
                }

                // Coach tab
                navigation(
                    route = CoachRoutes.GRAPH,
                    startDestination = CoachRoutes.SESSION_LIST,
                ) {
                    composable(CoachRoutes.SESSION_LIST) {
                        StubScreen("Coach Sessions — Sprint 14")
                    }
                    composable(CoachRoutes.CHAT) {
                        StubScreen("Chat — Sprint 14")
                    }
                }

                // Profile tab
                navigation(
                    route = ProfileRoutes.GRAPH,
                    startDestination = ProfileRoutes.HUB,
                ) {
                    composable(ProfileRoutes.HUB) {
                        StubScreen("Profile Hub — Sprint 13")
                    }
                    composable(ProfileRoutes.EDIT) {
                        StubScreen("Edit Profile — Sprint 4")
                    }
                    composable(ProfileRoutes.DASHBOARD) {
                        StubScreen("Profile Dashboard — Sprint 8")
                    }
                    composable(ProfileRoutes.BODY_WEIGHT) {
                        StubScreen("Body Weight — Sprint 8")
                    }
                    composable(ProfileRoutes.WEEKLY_SUMMARY) {
                        StubScreen("Weekly Summary — Sprint 8")
                    }
                    composable(ProfileRoutes.METABOLIC) {
                        StubScreen("Metabolic — Sprint 8")
                    }
                    composable(ProfileRoutes.EXPORT) {
                        StubScreen("Export — Sprint 13")
                    }
                    composable(ProfileRoutes.GAMIFICATION) {
                        StubScreen("Gamification — Sprint 11")
                    }
                    composable(ProfileRoutes.GLOSSARY) {
                        StubScreen("Glossary — Sprint 15")
                    }
                }
            }
        }
    }
}

@Composable
private fun StubScreen(label: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}




