package com.jlsh.aifit.feature.progression.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProgressionRecommendationResponseDto(
    val trainingExerciseId: String,
    val exerciseName: String,
    val type: String,
    val currentLoad: Double? = null,
    val suggestedLoad: Double? = null,
    val suggestedRepsMin: Int = 0,
    val suggestedRepsMax: Int = 0,
    val rationale: String,
    val confidence: Double,
    val basedOnSessions: Int,
)

