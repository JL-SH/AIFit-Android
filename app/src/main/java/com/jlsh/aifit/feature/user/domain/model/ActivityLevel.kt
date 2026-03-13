package com.jlsh.aifit.feature.user.domain.model

enum class ActivityLevel {
    SEDENTARY,
    LIGHT,
    MODERATE,
    ACTIVE,
    VERY_ACTIVE,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): ActivityLevel =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

