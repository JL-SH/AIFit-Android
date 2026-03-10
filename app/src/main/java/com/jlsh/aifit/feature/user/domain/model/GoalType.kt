package com.jlsh.aifit.feature.user.domain.model

enum class GoalType {
    LOSE_WEIGHT,
    GAIN_MUSCLE,
    MAINTAIN,
    IMPROVE_ENDURANCE,
    GENERAL_FITNESS,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): GoalType =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

