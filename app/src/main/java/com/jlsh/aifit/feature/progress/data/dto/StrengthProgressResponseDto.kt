package com.jlsh.aifit.feature.progress.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class StrengthProgressResponseDto(
    val exerciseName: String,
    val trainingExerciseId: String,
    val bestSetStart: BestSetResponseDto,
    val bestSetEnd: BestSetResponseDto,
    val progressionPercentage: Double,
    val trend: String,
)
