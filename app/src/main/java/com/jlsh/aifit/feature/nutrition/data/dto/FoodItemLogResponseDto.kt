package com.jlsh.aifit.feature.nutrition.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class FoodItemLogResponseDto(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val calories: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val fiberGrams: Double? = null,
)

