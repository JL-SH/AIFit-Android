package com.jlsh.aifit.feature.training.domain.model

enum class PlanStatus {
    ACTIVE,
    ARCHIVED,
    DELETED,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): PlanStatus =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

