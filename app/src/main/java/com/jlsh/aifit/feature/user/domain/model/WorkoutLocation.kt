package com.jlsh.aifit.feature.user.domain.model

enum class WorkoutLocation {
    GYM,
    HOME,
    OUTDOOR,
    HOME_GYM,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): WorkoutLocation =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

