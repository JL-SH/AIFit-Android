package com.jlsh.aifit.feature.progress.domain.model

import java.time.LocalDate

data class WeightEntry(
    val date: LocalDate,
    val weight: Double,
)

enum class WeightTrend {
    LOSING,
    GAINING,
    STABLE,
    INSUFFICIENT_DATA,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): WeightTrend =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

data class WeightProgress(
    val startWeight: Double?,
    val currentWeight: Double?,
    val targetWeight: Double?,
    val change: Double?,
    val trend: WeightTrend,
    val entries: List<WeightEntry>,
)

