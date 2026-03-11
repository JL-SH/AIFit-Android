package com.jlsh.aifit.feature.progress.domain.model

data class WorkoutAdherence(
    val plannedSessions: Int,
    val completedSessions: Int,
    val adherencePercentage: Double,
    val currentStreak: Int,
    val longestStreak: Int,
)

