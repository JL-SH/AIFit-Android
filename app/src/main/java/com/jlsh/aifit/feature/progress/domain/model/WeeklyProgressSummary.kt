package com.jlsh.aifit.feature.progress.domain.model

data class WeeklyProgressSummary(
    val workoutsThisWeek: Int,
    val workoutsTarget: Int,
    val averageCaloriesToday: Double?,
    val calorieTarget: Int,
    val currentStreak: Int,
    // ALL: delete if not used elsewhere — weight already displayed in BodyWeightScreen (UC-09)
    val bodyWeight: Double?,
)

