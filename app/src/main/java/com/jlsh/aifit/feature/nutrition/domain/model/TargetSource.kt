package com.jlsh.aifit.feature.nutrition.domain.model

enum class TargetSource {
    USER,
    METABOLIC_ADJUSTMENT,
    AI_GENERATED,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): TargetSource =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

