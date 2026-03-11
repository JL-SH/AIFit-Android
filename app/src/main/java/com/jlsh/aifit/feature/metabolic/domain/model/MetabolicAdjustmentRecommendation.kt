package com.jlsh.aifit.feature.metabolic.domain.model

enum class AdjustmentType {
    INCREASE_CALORIES,
    DECREASE_CALORIES,
    MAINTAIN,
    RECALCULATE,
    CALORIE_INCREASE,
    CALORIE_DECREASE,
    MAINTENANCE_HOLD,
    REVERSE_DIET,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): AdjustmentType =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

enum class AdjustmentMagnitude {
    SMALL,
    MODERATE,
    LARGE,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): AdjustmentMagnitude =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

enum class AdjustmentUrgency {
    LOW,
    MEDIUM,
    HIGH,
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

