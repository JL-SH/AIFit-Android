package com.jlsh.aifit.feature.workout.ui.state

sealed interface WorkoutSessionUiEvent {

    data object NavigateBack : WorkoutSessionUiEvent

    data class ShowSubstitutionSheet(val exerciseId: String) : WorkoutSessionUiEvent

    data class ShowSnackbar(val message: String) : WorkoutSessionUiEvent

    data object SessionAlreadyLocked : WorkoutSessionUiEvent
}

