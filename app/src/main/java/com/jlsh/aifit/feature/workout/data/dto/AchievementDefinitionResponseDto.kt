package com.jlsh.aifit.feature.workout.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AchievementDefinitionResponseDto(
    val id: String,
    val code: String,
    val type: String,
    val name: String,
    val description: String,
    val rarity: String,
    val iconKey: String,
)

