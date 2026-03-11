package com.jlsh.aifit.feature.nutrition.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateNutritionTargetRequestDto(
    val calorieTarget: Int? = null,
    val proteinTarget: Double? = null,
    val carbsTarget: Double? = null,
    val fatTarget: Double? = null,
)

