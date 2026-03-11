package com.jlsh.aifit.feature.workout.domain.model

data class GamificationResult(
    val newPersonalRecords: List<PersonalRecord>,
    val unlockedAchievements: List<Achievement>,
    val updatedStreakCount: Int?,
)

