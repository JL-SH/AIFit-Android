package com.jlsh.aifit.feature.workout.ui.state

import com.jlsh.aifit.feature.workout.domain.model.GamificationResult

sealed class WorkoutUiEvent {
    data class NavigateToDetail(val logId: String) : WorkoutUiEvent()
    data object NavigateBack : WorkoutUiEvent()
    data class SessionSaved(val gamificationResult: GamificationResult?) : WorkoutUiEvent()
    data class ShowAchievementDialog(
        val code: String,
        val fallbackName: String,
        val fallbackDescription: String,
    ) : WorkoutUiEvent()
    data class ShowSnackbar(val message: String) : WorkoutUiEvent()
    data object DiscardConfirmation : WorkoutUiEvent()
}

