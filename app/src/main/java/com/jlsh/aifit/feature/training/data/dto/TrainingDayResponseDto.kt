package com.jlsh.aifit.feature.training.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TrainingDayResponseDto(
    val id: String,
    val dayNumber: Int,
    val name: String,
    val estimatedDurationMinutes: Int,
    val exercises: List<TrainingExerciseResponseDto> = emptyList(),
    val dayOfWeek: String? = null,
    val dayType: String? = null,
)

