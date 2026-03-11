package com.jlsh.aifit.feature.progress.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutAdherenceResponseDto(
    val plannedSessions: Int,
    val completedSessions: Int,
    val adherencePercentage: Double,
    val currentStreak: Int,
    val longestStreak: Int,
    val weeklyBreakdown: List<WeeklyAdherenceDto> = emptyList(),
)

