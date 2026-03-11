package com.jlsh.aifit.feature.metabolic.domain.model

enum class MetabolicStatus {
    OPTIMAL,
    PLATEAU,
    CUTTING_TOO_FAST,
    BULKING_TOO_FAST,
    INSUFFICIENT_DATA,
    MAINTENANCE_DEVIATION,
    ON_TRACK,
    SLIGHT_DEVIATION,
    SIGNIFICANT_DEVIATION,
    REVERSE_TREND,
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

