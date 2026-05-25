package com.jlsh.aifit.core.ui.components.plans

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jlsh.aifit.core.ui.components.feedback.LoadingScreen

/**
 * Full screen loading screen when changing active plan.
 * Uses the same look as [LoadingScreen] (opaque background, centered spinner) to avoid
 * show the hub behind or a layout different from the rest of the app.
 */
@Composable
fun PlanActivatingOverlay(modifier: Modifier = Modifier) {
    LoadingScreen(modifier = modifier)
}

@Composable
fun TrainingPlanActivatingOverlay(modifier: Modifier = Modifier) {
    PlanActivatingOverlay(modifier = modifier)
}

@Composable
fun DietPlanActivatingOverlay(modifier: Modifier = Modifier) {
    PlanActivatingOverlay(modifier = modifier)
}
