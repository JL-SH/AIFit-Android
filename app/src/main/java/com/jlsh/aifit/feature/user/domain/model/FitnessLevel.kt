package com.jlsh.aifit.feature.user.domain.model

enum class FitnessLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    ELITE,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): FitnessLevel =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

