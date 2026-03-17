package com.jlsh.aifit.feature.workout.ui.state

import com.jlsh.aifit.feature.training.domain.model.ExerciseSubstitution
import com.jlsh.aifit.feature.training.domain.model.MuscleGroup
import com.jlsh.aifit.feature.workout.domain.model.WorkoutSetLog

data class WorkoutSessionData(
    val exercises: List<SessionExercise>,
    val currentExerciseIndex: Int,
    val registeredSets: List<WorkoutSetLog>,
    val autoregulationSuggestion: Double?,
    val restTimerSeconds: Int?,
    val volumeByMuscleGroup: Map<MuscleGroup, Double>,
    val ghostSets: List<WorkoutSetLog>,
    val substitutions: List<ExerciseSubstitution>?,
)
