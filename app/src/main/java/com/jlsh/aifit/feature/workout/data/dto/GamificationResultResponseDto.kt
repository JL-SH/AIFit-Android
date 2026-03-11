package com.jlsh.aifit.feature.workout.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GamificationResultResponseDto(
    val newPersonalRecords: List<PersonalRecordResponseDto> = emptyList(),
    val unlockedAchievements: List<UserAchievementResponseDto> = emptyList(),
    val updatedStreak: StreakResponseDto? = null,
)

