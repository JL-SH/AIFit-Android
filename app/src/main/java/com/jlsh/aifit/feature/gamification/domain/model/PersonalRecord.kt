package com.jlsh.aifit.feature.gamification.domain.model

data class PersonalRecord(
    val id: String,
    val exerciseName: String,
    val weightKg: Double,
    val reps: Int,
    val estimatedOneRepMax: Double,
    val achievedAt: String,
)

