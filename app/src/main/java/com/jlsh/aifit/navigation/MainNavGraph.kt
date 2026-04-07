package com.jlsh.aifit.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
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
import com.jlsh.aifit.feature.chat.ui.ChatScreen
import com.jlsh.aifit.feature.chat.ui.ChatSessionListScreen
import com.jlsh.aifit.feature.diet.ui.DietDetailScreen
import com.jlsh.aifit.feature.diet.ui.DietPlanApprovalScreen
import com.jlsh.aifit.feature.diet.ui.GenerateDietScreen
import com.jlsh.aifit.feature.education.ui.GlossaryScreen
import com.jlsh.aifit.feature.gamification.ui.GamificationScreen
import com.jlsh.aifit.feature.gamification.ui.ProgressExportScreen
import com.jlsh.aifit.feature.home.ui.HomeScreen
import com.jlsh.aifit.feature.metabolic.ui.MetabolicAnalysisScreen
import com.jlsh.aifit.feature.nutrition.ui.NutritionHubScreen
import com.jlsh.aifit.feature.nutrition.ui.NutritionTargetScreen
import com.jlsh.aifit.feature.nutrition.ui.TrackMealScreen
import com.jlsh.aifit.feature.progress.ui.BodyWeightScreen
import com.jlsh.aifit.feature.progress.ui.ProgressDashboardScreen
import com.jlsh.aifit.feature.progress.ui.WeeklySummaryScreen
import com.jlsh.aifit.feature.shopping.ui.ShoppingDetailScreen
import com.jlsh.aifit.feature.training.ui.GeneratePlanScreen
import com.jlsh.aifit.feature.training.ui.TrainingDetailScreen
import com.jlsh.aifit.feature.training.ui.TrainingHubScreen
import com.jlsh.aifit.feature.training.ui.TrainingPlanApprovalScreen
import com.jlsh.aifit.feature.user.ui.ProfileHubScreen
import com.jlsh.aifit.feature.user.ui.UserProfileScreen
import com.jlsh.aifit.feature.vision.ui.FoodVisionScreen
import com.jlsh.aifit.feature.workout.ui.WorkoutDetailScreen
import com.jlsh.aifit.feature.workout.ui.WorkoutLogScreen
import com.jlsh.aifit.feature.workout.ui.WorkoutSessionScreen

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

    val currentTabRoute = bottomNavItems.firstOrNull { item ->
        currentRoute?.startsWith(item.route) == true
    }?.route

    // Routes where bottom bar should be hidden (focus mode)
    val hideBottomBarRoutes = listOf(
        "training/workout_log",
        "training/session/",
        "training/generate",        // training/generate?adaptive=...
        "training/approval/",       // training/approval/{planId}
        "nutrition/diet_generate",  // nutrition/diet_generate?adaptive=...
        "nutrition/diet/approval/", // nutrition/diet/approval/{planId}
        "coach/chat/",
        "nutrition/food_vision",
    )
    val isBottomBarVisible = hideBottomBarRoutes.none { prefix ->
        currentRoute?.startsWith(prefix) == true
    }

    CompositionLocalProvider(LocalBottomBarVisibility provides isBottomBarVisible) {
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
                // ── Home tab ─────────────────────────────────────────────
                navigation(
                    route = HomeRoutes.GRAPH,
                    startDestination = HomeRoutes.HOME,
                ) {
                    composable(HomeRoutes.HOME) {
                        HomeScreen(
                            onNavigateToWorkoutSession = { planId, dayId ->
                                tabNavController.navigate(TrainingRoutes.workoutSessionRoute(planId, dayId)) {
                                    popUpTo(HomeRoutes.GRAPH) { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToTrackMeal = {
                                tabNavController.navigate(NutritionRoutes.trackMealRoute()) {
                                    popUpTo(HomeRoutes.GRAPH) { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToProgressDashboard = {
                                tabNavController.navigate(HomeRoutes.DASHBOARD)
                            },
                            onNavigateToBodyWeight = {
                                tabNavController.navigate(HomeRoutes.BODY_WEIGHT)
                            },
                            onNavigateToGamification = { tab ->
                                tabNavController.navigate(ProfileRoutes.gamificationRoute(tab)) {
                                    popUpTo(HomeRoutes.GRAPH) { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToProfile = {
                                tabNavController.navigate(ProfileRoutes.GRAPH) {
                                    popUpTo(tabNavController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onNavigateToGeneratePlan = {
                                tabNavController.navigate(TrainingRoutes.generateRoute()) {
                                    popUpTo(HomeRoutes.GRAPH) { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToTrainingDetail = { planId ->
                                tabNavController.navigate(TrainingRoutes.detailRoute(planId)) {
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                    composable(HomeRoutes.DASHBOARD) {
                        ProgressDashboardScreen(
                            onNavigateBack = { tabNavController.popBackStack() },
                            onNavigateToBodyWeight = {
                                tabNavController.navigate(HomeRoutes.BODY_WEIGHT)
                            },
                            onNavigateToWeeklySummary = {
                                tabNavController.navigate(HomeRoutes.WEEKLY_SUMMARY)
                            },
                            onNavigateToMetabolic = {
                                tabNavController.navigate(HomeRoutes.METABOLIC_ANALYSIS)
                            },
                        )
                    }
                    composable(HomeRoutes.BODY_WEIGHT) {
                        BodyWeightScreen(
                            onNavigateBack = { tabNavController.popBackStack() },
                        )
                    }
                    composable(HomeRoutes.WEEKLY_SUMMARY) {
                        WeeklySummaryScreen(
                            onNavigateBack = { tabNavController.popBackStack() },
                        )
                    }
                    composable(HomeRoutes.METABOLIC_ANALYSIS) {
                        MetabolicAnalysisScreen(
                            onNavigateBack = { tabNavController.popBackStack() },
                        )
                    }
                }

                // ── Training tab ─────────────────────────────────────────
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
                            onNavigateToWorkoutDetail = { logId ->
                                tabNavController.navigate(TrainingRoutes.workoutDetailRoute(logId))
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
                            onNavigateToSession = { pId, dId ->
                                tabNavController.navigate(TrainingRoutes.workoutSessionRoute(pId, dId))
                            },
                        )
                    }
                    composable(
                        route = TrainingRoutes.WORKOUT_SESSION,
                        arguments = listOf(
                            navArgument("planId") { type = NavType.StringType },
                            navArgument("dayId") { type = NavType.StringType },
                        ),
                    ) {
                        WorkoutSessionScreen(
                            onNavigateBack = {
                                // Abandon session — clear the entire back stack and return to Home
                                // so the user never lands on a stale Training screen.
                                tabNavController.navigate(HomeRoutes.GRAPH) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onSessionFinalized = { logId ->
                                tabNavController.navigate(HomeRoutes.GRAPH) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                                tabNavController.navigate(TrainingRoutes.workoutDetailRoute(logId))
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
                                tabNavController.navigate(TrainingRoutes.approvalRoute(newPlanId)) {
                                    popUpTo(TrainingRoutes.GENERATE) { inclusive = true }
                                }
                            },
                        )
                    }
                    composable(
                        route = TrainingRoutes.APPROVAL,
                        arguments = listOf(
                            navArgument("planId") { type = NavType.StringType },
                        ),
                    ) { backStackEntry ->
                        val planId = backStackEntry.arguments?.getString("planId") ?: return@composable
                        TrainingPlanApprovalScreen(
                            planId = planId,
                            onAccept = {
                                tabNavController.navigate(TrainingRoutes.HUB) {
                                    popUpTo(TrainingRoutes.GRAPH) { inclusive = false }
                                }
                            },
                            onNavigateToApproval = { newPlanId ->
                                tabNavController.navigate(TrainingRoutes.approvalRoute(newPlanId)) {
                                    popUpTo(TrainingRoutes.APPROVAL) { inclusive = true }
                                }
                            },
                        )
                    }
                    composable(
                        route = TrainingRoutes.WORKOUT_LOG,
                        arguments = listOf(
                            navArgument("planId") {
                                type = NavType.StringType
                                defaultValue = ""
                            },
                        ),
                    ) { backStackEntry ->
                        val planId = backStackEntry.arguments?.getString("planId") ?: ""
                        WorkoutLogScreen(
                            planId = planId,
                            onNavigateBack = { tabNavController.popBackStack() },
                            onNavigateToDetail = { logId ->
                                tabNavController.navigate(TrainingRoutes.workoutDetailRoute(logId)) {
                                    popUpTo(TrainingRoutes.WORKOUT_LOG) { inclusive = true }
                                }
                            },
                        )
                    }
                    composable(
                        route = TrainingRoutes.WORKOUT_DETAIL,
                        arguments = listOf(
                            navArgument("logId") { type = NavType.StringType },
                        ),
                    ) { backStackEntry ->
                        val logId = backStackEntry.arguments?.getString("logId") ?: ""
                        WorkoutDetailScreen(
                            logId = logId,
                            onNavigateBack = {
                                if (!tabNavController.popBackStack()) {
                                    tabNavController.navigate(TrainingRoutes.HUB) {
                                        popUpTo(tabNavController.graph.startDestinationId) { inclusive = true }
                                    }
                                }
                            },
                        )
                    }
                }

                // ── Nutrition tab ────────────────────────────────────────
                navigation(
                    route = NutritionRoutes.GRAPH,
                    startDestination = NutritionRoutes.HUB,
                ) {
                    composable(NutritionRoutes.HUB) {
                        NutritionHubScreen(
                            onNavigateToTrackMeal = { mode ->
                                tabNavController.navigate(NutritionRoutes.trackMealRoute(mode = mode))
                            },
                            onNavigateToFoodVision = {
                                tabNavController.navigate(NutritionRoutes.FOOD_VISION)
                            },
                            onNavigateToNutritionTarget = {
                                tabNavController.navigate(NutritionRoutes.TARGET)
                            },
                            onNavigateToDietDetail = { planId ->
                                tabNavController.navigate(NutritionRoutes.dietDetailRoute(planId))
                            },
                            onNavigateToGenerateDiet = {
                                tabNavController.navigate(NutritionRoutes.dietGenerateRoute())
                            },
                            onNavigateToShoppingDetail = { listId ->
                                tabNavController.navigate(NutritionRoutes.shoppingDetailRoute(listId))
                            },
                        )
                    }
                    composable(
                        route = NutritionRoutes.TRACK_MEAL,
                        arguments = listOf(
                            navArgument("mode") {
                                type = NavType.StringType
                                defaultValue = ""
                            },
                            navArgument("prefilled") {
                                type = NavType.StringType
                                defaultValue = ""
                            },
                        ),
                    ) { backStackEntry ->
                        val mode = backStackEntry.arguments?.getString("mode") ?: ""
                        TrackMealScreen(
                            mode = mode,
                            onNavigateBack = { tabNavController.popBackStack() },
                            onNavigateToHome = {
                                tabNavController.navigate(HomeRoutes.GRAPH) {
                                    popUpTo(tabNavController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                    composable(NutritionRoutes.FOOD_VISION) {
                        FoodVisionScreen(
                            onNavigateBack = { tabNavController.popBackStack() },
                            onNavigateToTrackMeal = { prefilled ->
                                tabNavController.navigate(
                                    NutritionRoutes.trackMealRoute(mode = "manual", prefilled = prefilled)
                                )
                            },
                        )
                    }
                    composable(NutritionRoutes.TARGET) {
                        NutritionTargetScreen(
                            onNavigateBack = { tabNavController.popBackStack() },
                        )
                    }
                    composable(
                        route = NutritionRoutes.DIET_DETAIL,
                        arguments = listOf(
                            navArgument("planId") { type = NavType.StringType },
                        ),
                    ) { backStackEntry ->
                        val planId = backStackEntry.arguments?.getString("planId") ?: ""
                        DietDetailScreen(
                            planId = planId,
                            onNavigateBack = { tabNavController.popBackStack() },
                            onNavigateToGenerate = { adaptive, basePlanId ->
                                tabNavController.navigate(NutritionRoutes.dietGenerateRoute(adaptive, basePlanId))
                            },
                        )
                    }
                    composable(
                        route = NutritionRoutes.DIET_GENERATE,
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
                        GenerateDietScreen(
                            adaptive = adaptive,
                            basePlanId = basePlanId,
                            onNavigateBack = { tabNavController.popBackStack() },
                            onNavigateToDetail = { newPlanId ->
                                tabNavController.navigate(NutritionRoutes.dietApprovalRoute(newPlanId)) {
                                    popUpTo(NutritionRoutes.DIET_GENERATE) { inclusive = true }
                                }
                            },
                        )
                    }
                    composable(
                        route = NutritionRoutes.DIET_APPROVAL,
                        arguments = listOf(
                            navArgument("planId") { type = NavType.StringType },
                        ),
                    ) { backStackEntry ->
                        val planId = backStackEntry.arguments?.getString("planId") ?: return@composable
                        DietPlanApprovalScreen(
                            planId = planId,
                            onAccept = {
                                tabNavController.navigate(NutritionRoutes.HUB) {
                                    popUpTo(NutritionRoutes.GRAPH) { inclusive = false }
                                }
                            },
                            onNavigateToApproval = { newPlanId ->
                                tabNavController.navigate(NutritionRoutes.dietApprovalRoute(newPlanId)) {
                                    popUpTo(NutritionRoutes.DIET_APPROVAL) { inclusive = true }
                                }
                            },
                        )
                    }
                    composable(
                        route = NutritionRoutes.SHOPPING_DETAIL,
                        arguments = listOf(
                            navArgument("listId") { type = NavType.StringType },
                        ),
                    ) {
                        ShoppingDetailScreen(
                            onNavigateBack = { tabNavController.popBackStack() },
                        )
                    }
                }

                // ── Coach tab ────────────────────────────────────────────
                navigation(
                    route = CoachRoutes.GRAPH,
                    startDestination = CoachRoutes.SESSION_LIST,
                ) {
                    composable(CoachRoutes.SESSION_LIST) {
                        ChatSessionListScreen(
                            onNavigateToChat = { sessionId ->
                                tabNavController.navigate(CoachRoutes.chatRoute(sessionId))
                            },
                        )
                    }
                    composable(
                        route = CoachRoutes.CHAT,
                        arguments = listOf(
                            navArgument("sessionId") { type = NavType.StringType },
                        ),
                    ) {
                        ChatScreen(
                            onNavigateBack = { tabNavController.popBackStack() },
                        )
                    }
                }

                // ── Profile tab ──────────────────────────────────────────
                navigation(
                    route = ProfileRoutes.GRAPH,
                    startDestination = ProfileRoutes.HUB,
                ) {
                    composable(ProfileRoutes.HUB) {
                        ProfileHubScreen(
                            onNavigateToEditProfile = {
                                tabNavController.navigate(ProfileRoutes.EDIT)
                            },
                            onNavigateToDashboard = {
                                tabNavController.navigate(ProfileRoutes.DASHBOARD)
                            },
                            onNavigateToBodyWeight = {
                                tabNavController.navigate(ProfileRoutes.BODY_WEIGHT)
                            },
                            onNavigateToMetabolic = {
                                tabNavController.navigate(ProfileRoutes.METABOLIC)
                            },
                            onNavigateToExport = {
                                tabNavController.navigate(ProfileRoutes.EXPORT)
                            },
                            onNavigateToGamification = { tab ->
                                tabNavController.navigate(ProfileRoutes.gamificationRoute(tab))
                            },
                            onNavigateToGlossary = {
                                tabNavController.navigate(ProfileRoutes.GLOSSARY)
                            },
                        )
                    }
                    composable(ProfileRoutes.EDIT) {
                        UserProfileScreen(
                            onNavigateBack = { tabNavController.popBackStack() },
                        )
                    }
                    composable(ProfileRoutes.DASHBOARD) {
                        ProgressDashboardScreen(
                            onNavigateBack = { tabNavController.popBackStack() },
                            onNavigateToBodyWeight = {
                                tabNavController.navigate(ProfileRoutes.BODY_WEIGHT)
                            },
                            onNavigateToWeeklySummary = {
                                tabNavController.navigate(ProfileRoutes.WEEKLY_SUMMARY)
                            },
                            onNavigateToMetabolic = {
                                tabNavController.navigate(ProfileRoutes.METABOLIC)
                            },
                        )
                    }
                    composable(ProfileRoutes.BODY_WEIGHT) {
                        BodyWeightScreen(
                            onNavigateBack = { tabNavController.popBackStack() },
                        )
                    }
                    composable(ProfileRoutes.WEEKLY_SUMMARY) {
                        WeeklySummaryScreen(
                            onNavigateBack = { tabNavController.popBackStack() },
                        )
                    }
                    composable(ProfileRoutes.METABOLIC) {
                        MetabolicAnalysisScreen(
                            onNavigateBack = { tabNavController.popBackStack() },
                        )
                    }
                    composable(ProfileRoutes.EXPORT) {
                        ProgressExportScreen(
                            onNavigateBack = { tabNavController.popBackStack() },
                        )
                    }
                    composable(
                        route = ProfileRoutes.GAMIFICATION,
                        arguments = listOf(
                            navArgument("tab") {
                                type = NavType.StringType
                                defaultValue = "ACHIEVEMENTS"
                            },
                        ),
                    ) {
                        GamificationScreen(
                            onNavigateBack = { tabNavController.popBackStack() },
                            onNavigateToExport = {
                                tabNavController.navigate(ProfileRoutes.EXPORT)
                            },
                        )
                    }
                    composable(ProfileRoutes.GLOSSARY) {
                        GlossaryScreen(
                            onNavigateBack = { tabNavController.popBackStack() },
                        )
                    }
                }
            }
        }
    }
}


