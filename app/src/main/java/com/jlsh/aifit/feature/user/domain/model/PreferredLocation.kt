package com.jlsh.aifit.feature.user.domain.model

enum class PreferredLocation {
    GYM,
    HOME,
    OUTDOOR,
    ANY,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): PreferredLocation =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

