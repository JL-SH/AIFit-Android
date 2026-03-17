package com.jlsh.aifit.feature.training.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class WarmUpExerciseResponseDto(
    val name: String,
    val description: String,
    val sets: Int,
    val reps: Int,
    val durationSeconds: Int? = null,
)

