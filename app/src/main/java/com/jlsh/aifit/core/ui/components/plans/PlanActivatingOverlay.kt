package com.jlsh.aifit.core.ui.components.plans

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jlsh.aifit.core.ui.components.feedback.LoadingScreen

/**
 * Pantalla de carga a pantalla completa al cambiar el plan activo.
 * Usa el mismo aspecto que [LoadingScreen] (fondo opaco, spinner centrado) para no
 * mostrar el hub detrás ni un layout distinto al resto de la app.
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
