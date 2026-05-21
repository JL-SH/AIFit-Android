package com.jlsh.aifit.feature.diet.ui.state

import com.jlsh.aifit.core.ui.components.layout.UiStateHost
import com.jlsh.aifit.feature.diet.domain.model.DietPlan

/**
 * Estado de la UI para la pantalla de detalle de un plan de dieta.
 */
sealed class DietUiState {
    /** Cargando el detalle del plan desde el repositorio. */
    data object Loading : DietUiState(), UiStateHost.Loading

    /** Regenerando el plan tras rechazo o feedback del usuario. */
    data object Regenerating : DietUiState()

    /**
     * Error al cargar o procesar el plan.
     *
     * @property message Mensaje de error para mostrar al usuario.
     */
    data class Error(override val message: String) : DietUiState(), UiStateHost.Error

    /**
     * Detalle del plan cargado correctamente.
     *
     * @property plan Plan de dieta con días y comidas.
     */
    data class Success(
        val plan: DietPlan,
    ) : DietUiState(), UiStateHost.Success
}

/**
 * Estado de la UI para el flujo de generación de un plan de dieta.
 */
sealed class GenerateDietUiState {
    /** Sin generación en curso; formulario listo para enviar. */
    data object Idle : GenerateDietUiState()

    /** Generación en curso; se muestra animación de carga. */
    data object Generating : GenerateDietUiState()

    /**
     * Error durante la generación.
     *
     * @property message Mensaje de error para mostrar al usuario.
     */
    data class Error(val message: String) : GenerateDietUiState()

    /**
     * Plan generado correctamente.
     *
     * @property plan Plan recién creado.
     */
    data class Success(val plan: DietPlan) : GenerateDietUiState()
}
