package com.jlsh.aifit.feature.progression.domain.model

enum class ProgressionType {
    WEIGHT_INCREASE,
    REP_INCREASE,
    SET_INCREASE,
    MAINTAIN,
    DELOAD,
    INSUFFICIENT_DATA,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): ProgressionType =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

data class ProgressionRecommendation(
    val trainingExerciseId: String,
    val exerciseName: String,
    val type: ProgressionType,
    val currentLoad: Double?,
    val suggestedLoad: Double?,
    val suggestedRepsMin: Int,
    val suggestedRepsMax: Int,
    val rationale: String,
    val confidence: Double,
    val basedOnSessions: Int,
)

