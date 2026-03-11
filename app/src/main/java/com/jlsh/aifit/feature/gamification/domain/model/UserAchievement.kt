package com.jlsh.aifit.feature.gamification.domain.model

data class UserAchievement(
    val id: String,
    val achievement: AchievementDefinition,
    val unlockedAt: String,
    val triggerDescription: String,
)

