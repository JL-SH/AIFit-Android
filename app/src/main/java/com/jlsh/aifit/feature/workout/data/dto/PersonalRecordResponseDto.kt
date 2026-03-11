package com.jlsh.aifit.feature.workout.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class PersonalRecordResponseDto(
    val id: String,
    val exerciseName: String,
    val weightKg: Double,
    val reps: Int,
    val estimatedOneRepMax: Double,
    val achievedAt: String,
)

