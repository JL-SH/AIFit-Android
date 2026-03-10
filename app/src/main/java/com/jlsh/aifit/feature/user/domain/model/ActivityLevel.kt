package com.jlsh.aifit.feature.user.domain.model

enum class ActivityLevel {
    SEDENTARY,
    LIGHTLY_ACTIVE,
    MODERATELY_ACTIVE,
    VERY_ACTIVE,
    EXTREMELY_ACTIVE,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): ActivityLevel =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

