package com.jlsh.aifit.feature.gamification.domain.model

enum class AchievementType {
    STRENGTH_MILESTONE,
    ADHERENCE_STREAK,
    NUTRITION_CONSISTENCY,
    KNOWLEDGE_ACQUIRED,
    WEIGHT_GOAL,
    FIRST_PLAN_COMPLETED,
    CONSECUTIVE_WEEKS,
    PERSONAL_RECORD,
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

