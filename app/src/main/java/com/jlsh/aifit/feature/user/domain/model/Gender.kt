package com.jlsh.aifit.feature.user.domain.model

enum class Gender {
    MALE,
    FEMALE,
    OTHER,
    PREFER_NOT_TO_SAY,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): Gender =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

