package com.jlsh.aifit.feature.gamification.data.dto

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

