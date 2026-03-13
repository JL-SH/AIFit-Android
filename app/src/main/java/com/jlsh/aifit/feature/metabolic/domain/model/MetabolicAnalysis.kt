package com.jlsh.aifit.feature.metabolic.domain.model

enum class MetabolicStatus {
    ON_TRACK,
    STAGNATED,
    UNDER_EATING_SIGNAL,
    OVER_EATING_SIGNAL,
    PROGRESSING_TOO_FAST,
    INSUFFICIENT_DATA,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): MetabolicStatus =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

data class MetabolicAnalysis(
    val status: MetabolicStatus,
    val weightTrend: WeightTrend,
    val calorieAdherenceRate: Double,
    val averageCalorieDeficitSurplus: Double,
    val recommendation: MetabolicAdjustmentRecommendation?,
    val rationale: String,
)

