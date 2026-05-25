package com.jlsh.aifit.feature.workout.ui.state

import com.jlsh.aifit.feature.training.domain.model.WarmUpProtocol
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog

/**
 * UI states of a training session in progress or just completed.
 */
sealed interface WorkoutSessionUiState {

    /** Initial state before loading plan and day from navigation arguments.*/
    data object Idle : WorkoutSessionUiState

    /** Loading exercises from the warm-up plan and protocol.*/
    data object LoadingWarmUp : WorkoutSessionUiState

    /**
     * Warming ready to display on bottom sheet.
     *
     * @property protocol Warm-up protocol returned by the backend.
     */
    data class WarmUpReady(val protocol: WarmUpProtocol) : WorkoutSessionUiState

    /**
     * Active session: record of series, timer and accumulated volume.
     *
     * @property sessionData Mutable data of the current session.
     */
    data class SessionActive(val sessionData: WorkoutSessionData) : WorkoutSessionUiState

    /** Sending fatigue and joint pain to the backend to log out.*/
    data object Finalizing : WorkoutSessionUiState

    /**
     * Successfully logged out; the screen navigates to the summary.
     *
     * @property summary Finalized log returned by the server.
     */
    data class SessionFinalized(val summary: WorkoutLog) : WorkoutSessionUiState

    /**
     * Unrecoverable error loading or preparing session.
     *
     * @property message Message to display to the user.
     */
    data class Error(val message: String) : WorkoutSessionUiState
}
