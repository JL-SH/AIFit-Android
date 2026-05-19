package com.jlsh.aifit.feature.gamification.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class PersonalRecordResponseDto(
    val id: String = "",
    val exerciseName: String = "",
    val weightKg: Double = 0.0,
    val reps: Int = 0,
    val estimatedOneRepMax: Double = 0.0,
    val achievedAt: String = "",
)
