package com.jlsh.aifit.feature.workout.domain.model

data class ExerciseProgressionHistory(
    val trainingExerciseId: String,
    val exerciseName: String,
    val entries: List<ExerciseProgressionEntry>,
)

data class ExerciseProgressionEntry(
    val date: String,
    val exerciseSetNumber: Int,
    val repsCompleted: Int,
    val weightUsed: Double,
)
