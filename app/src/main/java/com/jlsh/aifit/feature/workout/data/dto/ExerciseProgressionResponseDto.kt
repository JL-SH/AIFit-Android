package com.jlsh.aifit.feature.workout.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseProgressionResponseDto(
    val trainingExerciseId: String,
    val exerciseName: String,
    val entries: List<ProgressionEntryResponseDto> = emptyList(),
)

