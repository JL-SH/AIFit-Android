package com.jlsh.aifit.feature.training.ui.state

import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.training.domain.model.TrainingDay
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan

/**
 * Estado de la UI del hub de entrenamiento: plan activo, listado y filtros.
 */
sealed interface TrainingHubUiState {

    /** Carga inicial o refresco sin datos previos en pantalla. */
    data object Loading : TrainingHubUiState

    /**
     * Error al obtener los planes.
     *
     * @property message Mensaje de error para mostrar al usuario.
     */
    data class Error(val message: String) : TrainingHubUiState

    /** El usuario tiene planes guardados pero ninguno está activo. */
    data object NoActivePlan : TrainingHubUiState

    /**
     * Hay un plan activo con progreso semanal y listado de todos los planes.
     *
     * @property plan Plan actualmente activo.
     * @property currentWeek Semana actual dentro de la duración del plan (1-based).
     * @property nextDay Próximo día de entrenamiento sugerido, o null si no hay días cargados.
     * @property allPlans Todos los planes del usuario para el listado inferior.
     * @property selectedFilter Filtro de estado aplicado al listado; null muestra todos.
     */
    data class ActivePlan(
        val plan: TrainingPlan,
        val currentWeek: Int,
        val nextDay: TrainingDay?,
        val allPlans: List<TrainingPlan>,
        val selectedFilter: PlanStatus?,
    ) : TrainingHubUiState
}
