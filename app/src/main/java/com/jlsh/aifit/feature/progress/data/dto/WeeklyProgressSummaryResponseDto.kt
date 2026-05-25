package com.jlsh.aifit.feature.progress.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class WeeklyProgressSummaryResponseDto(
    val workoutsThisWeek: Int,
    val workoutsTarget: Int,
    val averageCaloriesToday: Double? = null,
    val calorieTarget: Int,
    val currentStreak: Int,
    val bodyWeight: Double? = null,
)

