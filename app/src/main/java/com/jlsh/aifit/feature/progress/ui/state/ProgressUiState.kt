package com.jlsh.aifit.feature.progress.ui.state

import com.jlsh.aifit.core.ui.components.layout.UiStateHost
import com.jlsh.aifit.feature.progress.domain.model.BodyWeightLog
import com.jlsh.aifit.feature.progress.domain.model.ProgressDashboard
import com.jlsh.aifit.feature.progress.domain.model.WeeklyProgressSummary
import java.time.LocalDate

/**
 * Estado de la pantalla principal del dashboard de progreso.
 */
sealed class DashboardUiState {
    /** Carga inicial o recarga del panel. */
    data object Loading : DashboardUiState(), UiStateHost.Loading

    /**
     * Error al obtener el dashboard.
     *
     * @property message Texto para mostrar al usuario.
     */
    data class Error(override val message: String) : DashboardUiState(), UiStateHost.Error

    /**
     * Dashboard cargado correctamente.
     *
     * @property dashboard Métricas agregadas del periodo seleccionado.
     * @property selectedPeriod Etiqueta del filtro temporal activo (p. ej. «30 días»).
     */
    data class Success(
        val dashboard: ProgressDashboard,
        val selectedPeriod: String = "30 days",
    ) : DashboardUiState(), UiStateHost.Success
}

/**
 * Estado de la pantalla de registro e historial de peso corporal.
 *
 * @property weightHistory Entradas de peso en el rango consultado.
 * @property isLoading Indica si el historial se está cargando.
 * @property formWeight Texto del campo de peso en el formulario.
 * @property formDate Fecha seleccionada para el nuevo registro.
 * @property formNotes Notas opcionales del registro.
 * @property isSaving Indica si un envío de peso está en curso.
 */
data class BodyWeightUiState(
    val weightHistory: List<BodyWeightLog> = emptyList(),
    val isLoading: Boolean = true,
    val formWeight: String = "",
    val formDate: LocalDate = LocalDate.now(),
    val formNotes: String = "",
    val isSaving: Boolean = false,
)

/**
 * Estado de la pantalla de resumen semanal de progreso.
 */
sealed class WeeklySummaryUiState {
    /** Carga del resumen semanal. */
    data object Loading : WeeklySummaryUiState(), UiStateHost.Loading

    /**
     * Error al obtener el resumen.
     *
     * @property message Texto para mostrar al usuario.
     */
    data class Error(override val message: String) : WeeklySummaryUiState(), UiStateHost.Error

    /**
     * Resumen semanal disponible.
     *
     * @property summary Datos agregados de la semana.
     */
    data class Success(
        val summary: WeeklyProgressSummary,
    ) : WeeklySummaryUiState(), UiStateHost.Success
}

