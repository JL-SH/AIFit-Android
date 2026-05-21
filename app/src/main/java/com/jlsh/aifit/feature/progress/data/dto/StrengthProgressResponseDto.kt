package com.jlsh.aifit.feature.progress.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class StrengthProgressResponseDto(
    val exerciseName: String,
    val trainingExerciseId: String,
    val bestSetStart: BestSetResponseDto? = null,
    val bestSetEnd: BestSetResponseDto? = null,
    val progressionPercentage: Double? = null,
    val trend: String? = null,
)
