package com.jlsh.aifit.feature.training.domain.model

enum class PlanStatus {
    ACTIVE,
    COMPLETED,
    PAUSED,
    DRAFT,
    DELETED,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): PlanStatus =
            value?.let { runCatching { valueOf(it.uppercase()) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

