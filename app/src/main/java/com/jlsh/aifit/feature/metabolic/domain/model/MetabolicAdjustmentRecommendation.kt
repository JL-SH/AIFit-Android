package com.jlsh.aifit.feature.metabolic.domain.model

enum class AdjustmentType {
    INCREASE_CALORIES,
    DECREASE_CALORIES,
    INCREASE_PROTEIN,
    REBALANCE_MACROS,
    MAINTAIN,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): AdjustmentType =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

enum class AdjustmentMagnitude {
    MINOR,
    MODERATE,
    SIGNIFICANT,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): AdjustmentMagnitude =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

enum class AdjustmentUrgency {
    INFORMATIONAL,
    SUGGESTED,
    RECOMMENDED,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): AdjustmentUrgency =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

data class MetabolicAdjustmentRecommendation(
    val type: AdjustmentType,
    val suggestedCalorieTarget: Int,
    val suggestedProteinTarget: Double,
    val suggestedCarbsTarget: Double,
    val suggestedFatTarget: Double,
    val magnitude: AdjustmentMagnitude,
    val urgency: AdjustmentUrgency,
)

