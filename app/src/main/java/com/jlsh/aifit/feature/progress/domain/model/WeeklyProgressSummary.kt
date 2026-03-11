package com.jlsh.aifit.feature.progress.domain.model

data class WeeklyProgressSummary(
    val workoutsThisWeek: Int,
    val workoutsTarget: Int,
    val averageCaloriesToday: Double,
    val calorieTarget: Int,
    val currentStreak: Int,
    val bodyWeight: Double?,
)

