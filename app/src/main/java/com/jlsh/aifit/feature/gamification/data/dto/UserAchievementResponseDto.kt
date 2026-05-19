package com.jlsh.aifit.feature.gamification.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserAchievementResponseDto(
    val id: String = "",
    val achievement: AchievementDefinitionResponseDto = AchievementDefinitionResponseDto(),
    val unlockedAt: String = "",
    val triggerDescription: String = "",
)
