package com.jlsh.aifit.feature.metabolic.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MetabolicAnalysisResponseDto(
    val status: String,
    val weightTrend: WeightTrendResponseDto? = null,
    val calorieAdherenceRate: Double,
    val averageCalorieDeficitSurplus: Double,
    val recommendation: MetabolicAdjustmentRecommendationResponseDto? = null,
    val rationale: String,
)

