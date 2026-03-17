package com.jlsh.aifit.feature.training.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class WarmUpProtocolResponseDto(
    val trainingDayId: String,
    val estimatedTotalLoad: Double,
    val exercises: List<WarmUpExerciseResponseDto>,
)

