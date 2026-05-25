package com.jlsh.aifit.feature.workout.ui.state

import com.jlsh.aifit.core.ui.components.layout.UiStateHost
import com.jlsh.aifit.feature.training.domain.model.TrainingDay
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import com.jlsh.aifit.feature.workout.domain.util.WorkoutSessionStats
import java.time.DayOfWeek

// --- Logging screen states ---

data class SetEntryState(
    val trainingExerciseId: String,
    val exerciseName: String,
    val exerciseSetNumber: Int,
    val repsCompleted: String = "",
    val weightUsed: String = "",
    val completed: Boolean = false,
)

sealed class LoggingUiState {
    data object Loading : LoggingUiState()
    data class Error(val message: String) : LoggingUiState()
    data class Ready(
        val planDay: TrainingDay,
        val setStates: List<SetEntryState>,
        val timerSeconds: Long = 0L,
        val isSaving: Boolean = false,
    ) : LoggingUiState() {
        val hasPendingSets: Boolean get() = setStates.any { it.completed }
    }
}

// --- History screen states ---

sealed class WorkoutHistoryUiState {
    data object Loading : WorkoutHistoryUiState(), UiStateHost.Loading
    data class Error(override val message: String) : WorkoutHistoryUiState(), UiStateHost.Error
    data class Success(
        val logs: List<WorkoutLog>,
        /** Logs grouped by "MMMM yyyy" key, sorted most-recent month first. */
        val logsByMonth: Map<String, List<WorkoutLog>> = emptyMap(),
        /** planId → planName lookup for display. */
        val planNameMap: Map<String, String> = emptyMap(),
        /** Currently selected day-of-week filter (null = all). */
        val selectedDayOfWeek: DayOfWeek? = null,
    ) : WorkoutHistoryUiState(), UiStateHost.Success
}

// --- Detail screen states ---

sealed class WorkoutDetailUiState {
    data object Loading : WorkoutDetailUiState(), UiStateHost.Loading
    data class Error(override val message: String) : WorkoutDetailUiState(), UiStateHost.Error
    data class Success(
        val log: WorkoutLog,
        val sessionStats: WorkoutSessionStats? = null,
    ) : WorkoutDetailUiState(), UiStateHost.Success {
        val totalVolume: Double
            get() = log.sets
                .filter { it.completed }
                .sumOf { set ->
                    val w = set.weightUsed ?: 0.0
                    val r = set.repsCompleted ?: 0
                    w * r
                }
    }
}

