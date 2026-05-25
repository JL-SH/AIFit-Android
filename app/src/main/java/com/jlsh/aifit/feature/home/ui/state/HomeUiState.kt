package com.jlsh.aifit.feature.home.ui.state

import com.jlsh.aifit.feature.gamification.domain.model.AchievementDefinition
import com.jlsh.aifit.feature.gamification.domain.model.Streak
import com.jlsh.aifit.feature.gamification.domain.model.UserAchievement
import com.jlsh.aifit.feature.progress.domain.model.BodyWeightLog
import com.jlsh.aifit.feature.progress.domain.model.WeeklyProgressSummary

/**
 * Estado de la pantalla de inicio (dashboard diario).
 */
sealed class HomeUiState {

    /** Carga inicial del dashboard; skeleton hasta snapshot completo. */
    data object Loading : HomeUiState()

    /**
     * Error irrecuperable en la carga (p. ej. perfil no disponible).
     *
     * @property message Mensaje de error para mostrar al usuario.
     */
    data class Error(val message: String) : HomeUiState()

    /**
     * Datos del dashboard listos para mostrar.
     *
     * @property userName Nombre del usuario para el saludo.
     * @property avatarUrl URL de la foto de perfil, o null si no hay imagen.
     * @property activePlan Resumen del plan de entrenamiento activo; null si no hay plan activo.
     * @property todayTraining Entrenamiento de hoy; null en día de descanso o sin plan.
     * @property todayNutrition Macros y calorías consumidas vs objetivo del día.
     * @property nextMeal Próxima comida del plan dietético activo.
     * @property streaks Rachas de gamificación (entrenamiento, nutrición, etc.).
     * @property weeklySummary Resumen de adherencia semanal; null hasta cargar en segundo plano.
     * @property weightEntries Últimos registros de peso (hasta 7 entradas recientes).
     * @property lastAchievement Último logro desbloqueado en los últimos 7 días.
     * @property nextAchievement Próximo logro pendiente de desbloquear.
     * @property trainingStreakDays Días consecutivos de racha de entrenamiento.
     * @property isTrainingHydrating Hay plan activo pero el detalle aún no está en caché (CTAs deshabilitados).
     */
    data class Success(
        val userName: String,
        val avatarUrl: String?,
        val activePlan: ActivePlanSummary? = null,
        val todayTraining: TodayTrainingState?,
        val todayNutrition: TodayNutritionState?,
        val nextMeal: NextMealState,
        val streaks: List<Streak>,
        val weeklySummary: WeeklyProgressSummary?,
        val weightEntries: List<BodyWeightLog>,
        val lastAchievement: UserAchievement? = null,
        val nextAchievement: AchievementDefinition? = null,
        val trainingStreakDays: Int = 0,
        val isTrainingHydrating: Boolean = false,
    ) : HomeUiState()
}

/**
 * Resumen ligero del plan de entrenamiento activo en [HomeUiState.Success].
 *
 * @property id Identificador del plan.
 * @property name Nombre visible del plan.
 */
data class ActivePlanSummary(
    val id: String,
    val name: String,
)

/**
 * Estado del entrenamiento programado para el día actual.
 *
 * @property planId Identificador del plan activo.
 * @property dayId Identificador del día de entrenamiento de hoy.
 * @property planName Nombre del plan.
 * @property dayName Nombre del día (p. ej. "Día 1 — Pecho").
 * @property exerciseCount Número total de ejercicios del día.
 * @property exerciseNames Nombres de ejercicios a mostrar en la tarjeta (recortados).
 * @property adherencePercentage Porcentaje de adherencia semanal al plan (0–100).
 * @property isCompleted True si el usuario ya completó y bloqueó el entreno de hoy.
 */
data class TodayTrainingState(
    val planId: String,
    val dayId: String,
    val planName: String,
    val dayName: String,
    val exerciseCount: Int,
    val exerciseNames: List<String>,
    val adherencePercentage: Float,
    val isCompleted: Boolean,
)

/**
 * Consumo nutricional del día frente a los objetivos configurados.
 *
 * @property caloriesConsumed Calorías registradas hoy.
 * @property calorieTarget Objetivo calórico diario.
 * @property proteinConsumed Proteína consumida (g).
 * @property proteinTarget Objetivo de proteína (g).
 * @property carbsConsumed Carbohidratos consumidos (g).
 * @property carbsTarget Objetivo de carbohidratos (g).
 * @property fatConsumed Grasas consumidas (g).
 * @property fatTarget Objetivo de grasas (g).
 */
data class TodayNutritionState(
    val caloriesConsumed: Int,
    val calorieTarget: Int,
    val proteinConsumed: Double,
    val proteinTarget: Double,
    val carbsConsumed: Double,
    val carbsTarget: Double,
    val fatConsumed: Double,
    val fatTarget: Double,
)

/**
 * Estado de la próxima comida según el plan dietético activo.
 */
sealed class NextMealState {

    /** No hay plan dietético activo o el plan no tiene días configurados. */
    data object NoPlan : NextMealState()

    /** Todas las comidas del día ya han pasado según la hora actual. */
    data object AllDone : NextMealState()

    /**
     * Hay una comida pendiente más adelante en el día.
     *
     * @property mealName Nombre de la comida.
     * @property estimatedTime Hora estimada (texto del plan o inferida por tipo).
     * @property calories Calorías de la comida.
     * @property proteinG Proteína en gramos.
     * @property carbsG Carbohidratos en gramos.
     * @property fatG Grasas en gramos.
     */
    data class Upcoming(
        val mealName: String,
        val estimatedTime: String,
        val calories: Int,
        val proteinG: Double,
        val carbsG: Double,
        val fatG: Double,
    ) : NextMealState()
}
