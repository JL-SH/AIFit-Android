package com.jlsh.aifit.feature.workout.domain.model

data class WorkoutSetLog(
    val id: String,
    val trainingExerciseId: String,
    val exerciseName: String,
    val exerciseSetNumber: Int,
    val repsCompleted: Int?,
    val weightUsed: Double?,
    val durationSeconds: Int?,
    val completed: Boolean,
    val estimatedOneRepMax: Double? = null,
    val wasAutoregulated: Boolean = false,
    val technicalNote: String? = null,
    val rpe: Int? = null,
)

