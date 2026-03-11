package com.jlsh.aifit.feature.diet.domain.model

enum class MealType {
    BREAKFAST,
    MORNING_SNACK,
    LUNCH,
    AFTERNOON_SNACK,
    DINNER,
    EVENING_SNACK,
    PRE_WORKOUT,
    POST_WORKOUT,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): MealType =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

