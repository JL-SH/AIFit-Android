package com.jlsh.aifit.feature.metabolic.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MetabolicAdjustmentRecommendationResponseDto(
    val type: String,
    val suggestedCalorieTarget: Int,
    val suggestedProteinTarget: Double,
    val suggestedCarbsTarget: Double,
    val suggestedFatTarget: Double,
    val magnitude: String,
    val urgency: String,
)

