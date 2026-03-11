package com.jlsh.aifit.feature.workout.domain.model

data class Achievement(
    val id: String,
    val code: String,
    val type: String,
    val name: String,
    val description: String,
    val rarity: String,
    val iconKey: String,
    val unlockedAt: String,
    val triggerDescription: String?,
)

