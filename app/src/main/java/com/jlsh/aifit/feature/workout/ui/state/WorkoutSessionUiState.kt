package com.jlsh.aifit.feature.workout.ui.state

import com.jlsh.aifit.feature.training.domain.model.WarmUpProtocol
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog

/**
 * Estados de la UI de una sesión de entrenamiento en curso o recién finalizada.
 */
sealed interface WorkoutSessionUiState {

    /** Estado inicial antes de cargar plan y día desde argumentos de navegación. */
    data object Idle : WorkoutSessionUiState

    /** Cargando ejercicios del plan y protocolo de calentamiento. */
    data object LoadingWarmUp : WorkoutSessionUiState

    /**
     * Calentamiento listo para mostrar en hoja inferior.
     *
     * @property protocol Protocolo de calentamiento devuelto por el backend.
     */
    data class WarmUpReady(val protocol: WarmUpProtocol) : WorkoutSessionUiState

    /**
     * Sesión activa: registro de series, temporizador y volumen acumulado.
     *
     * @property sessionData Datos mutables de la sesión en curso.
     */
    data class SessionActive(val sessionData: WorkoutSessionData) : WorkoutSessionUiState

    /** Enviando fatiga y dolor articular al backend para cerrar la sesión. */
    data object Finalizing : WorkoutSessionUiState

    /**
     * Sesión cerrada correctamente; la pantalla navega al resumen.
     *
     * @property summary Log finalizado devuelto por el servidor.
     */
    data class SessionFinalized(val summary: WorkoutLog) : WorkoutSessionUiState

    /**
     * Error irrecuperable al cargar o preparar la sesión.
     *
     * @property message Mensaje para mostrar al usuario.
     */
    data class Error(val message: String) : WorkoutSessionUiState
}
