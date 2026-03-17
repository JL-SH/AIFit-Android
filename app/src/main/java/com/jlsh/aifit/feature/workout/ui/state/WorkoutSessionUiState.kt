package com.jlsh.aifit.feature.workout.ui.state

import com.jlsh.aifit.feature.training.domain.model.WarmUpProtocol
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog

sealed interface WorkoutSessionUiState {

    data object Idle : WorkoutSessionUiState

    data object LoadingWarmUp : WorkoutSessionUiState

    data class WarmUpReady(val protocol: WarmUpProtocol) : WorkoutSessionUiState

    data class SessionActive(val sessionData: WorkoutSessionData) : WorkoutSessionUiState

    data object Finalizing : WorkoutSessionUiState

    data class SessionFinalized(val summary: WorkoutLog) : WorkoutSessionUiState

    data class Error(val message: String) : WorkoutSessionUiState
}

