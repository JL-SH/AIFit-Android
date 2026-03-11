package com.jlsh.aifit.feature.workout.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserAchievementResponseDto(
    val id: String,
    val achievement: AchievementDefinitionResponseDto,
    val unlockedAt: String,
    val triggerDescription: String? = null,
)

