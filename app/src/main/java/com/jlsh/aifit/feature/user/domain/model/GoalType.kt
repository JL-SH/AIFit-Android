package com.jlsh.aifit.feature.user.domain.model

enum class GoalType {
    LOSE_WEIGHT,
    GAIN_MUSCLE,
    MAINTAIN,
    BODY_RECOMPOSITION,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): GoalType =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

