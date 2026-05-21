package com.jlsh.aifit.feature.nutrition.ui.state

import com.jlsh.aifit.core.ui.components.layout.UiStateHost
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionLog
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget
import com.jlsh.aifit.feature.training.domain.model.PlanStatus

/**
 * Datos del día actual para la pestaña "Hoy" del hub de nutrición.
 *
 * @property nutritionLog Registro de comidas del día; null si aún no hay entradas.
 * @property target Objetivos calóricos y de macros vigentes; null si no están configurados.
 */
data class TodayState(
    val nutritionLog: NutritionLog? = null,
    val target: NutritionTarget? = null,
)

/**
 * Estado de la UI del hub de nutrición (pestañas Hoy, Plan de dieta y Compras).
 */
sealed class NutritionHubUiState {
    /** Cargando log del día, objetivos y lista de planes de dieta. */
    data object Loading : NutritionHubUiState(), UiStateHost.Loading

    /**
     * Error al cargar datos del hub.
     *
     * @property message Mensaje de error para mostrar al usuario.
     */
    data class Error(override val message: String) : NutritionHubUiState(), UiStateHost.Error

    /**
     * Hub cargado con datos del día y planes de dieta.
     *
     * @property todayState Resumen nutricional y objetivos del día actual.
     * @property dietPlans Lista de planes de dieta del usuario.
     * @property selectedTabIndex Índice de la pestaña seleccionada (0 = Hoy, 1 = Plan, 2 = Compras).
     * @property selectedDietPlanFilter Filtro por estado de plan; null muestra todos.
     * @property isActivatingPlan true mientras se confirma la activación de un plan en servidor.
     */
    data class Success(
        val todayState: TodayState,
        val dietPlans: List<DietPlan>,
        val selectedTabIndex: Int = 0,
        val selectedDietPlanFilter: PlanStatus? = null,
        val isActivatingPlan: Boolean = false,
    ) : NutritionHubUiState(), UiStateHost.Success
}

/**
 * Estado de la UI para registrar o analizar una comida.
 */
sealed class TrackMealUiState {
    /** Formulario listo; sin operación en curso. */
    data object Idle : TrackMealUiState()

    /** Guardando comida manual en el servidor. */
    data object Saving : TrackMealUiState()

    /** Analizando texto con IA antes de registrar la comida. */
    data object Analyzing : TrackMealUiState()

    /**
     * Error al guardar o analizar.
     *
     * @property message Mensaje de error para mostrar al usuario.
     */
    data class Error(val message: String) : TrackMealUiState()

    /** Comida registrada correctamente; la UI puede navegar al hub. */
    data object Saved : TrackMealUiState()
}

/**
 * Estado de la UI para editar los objetivos nutricionales del usuario.
 */
sealed class NutritionTargetUiState {
    /** Cargando objetivos actuales. */
    data object Loading : NutritionTargetUiState()

    /**
     * Error al cargar objetivos.
     *
     * @property message Mensaje de error para mostrar al usuario.
     */
    data class Error(val message: String) : NutritionTargetUiState()

    /**
     * Objetivos listos para editar en formulario.
     *
     * @property calorieTarget Objetivo calórico como texto editable.
     * @property proteinTarget Objetivo de proteína (g) como texto editable.
     * @property carbsTarget Objetivo de carbohidratos (g) como texto editable.
     * @property fatTarget Objetivo de grasas (g) como texto editable.
     * @property setBy Origen del objetivo (manual, plan de dieta, etc.) para mostrar al usuario.
     * @property isSaving true mientras se persiste la actualización en servidor.
     */
    data class Ready(
        val calorieTarget: String,
        val proteinTarget: String,
        val carbsTarget: String,
        val fatTarget: String,
        val setBy: String,
        val isSaving: Boolean = false,
    ) : NutritionTargetUiState()
}
