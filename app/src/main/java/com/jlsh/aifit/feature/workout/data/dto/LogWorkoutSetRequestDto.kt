package com.jlsh.aifit.feature.workout.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LogWorkoutSetRequestDto(
    val trainingExerciseId: String,
    val exerciseName: String,
    val exerciseSetNumber: Int,
    val repsCompleted: Int? = null,
    val weightUsed: Double? = null,
    val durationSeconds: Int? = null,
    val completed: Boolean,
)

