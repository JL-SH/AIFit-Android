package com.jlsh.aifit.feature.nutrition.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class NutritionTargetResponseDto(
    val id: String,
    val calorieTarget: Int,
    val proteinTarget: Double,
    val carbsTarget: Double,
    val fatTarget: Double,
    val effectiveFrom: String,
    val setBy: String,
)

