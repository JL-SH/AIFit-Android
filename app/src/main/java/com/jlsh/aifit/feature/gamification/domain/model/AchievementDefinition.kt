package com.jlsh.aifit.feature.gamification.domain.model

enum class AchievementType {
    WORKOUT_MILESTONE,
    NUTRITION_MILESTONE,
    STREAK,
    PERSONAL_RECORD,
    CONSISTENCY,
    SPECIAL,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): AchievementType =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

enum class AchievementRarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): AchievementRarity =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

data class AchievementDefinition(
    val id: String,
    val code: String,
    val type: AchievementType,
    val name: String,
    val description: String,
    val rarity: AchievementRarity,
    val iconKey: String,
)

