package com.jlsh.aifit.feature.gamification.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AchievementDefinitionResponseDto(
    val id: String = "",
    val code: String = "",
    val type: String = "UNKNOWN",
    val name: String = "",
    val description: String = "",
    val rarity: String = "UNKNOWN",
    val iconKey: String = "",
)
