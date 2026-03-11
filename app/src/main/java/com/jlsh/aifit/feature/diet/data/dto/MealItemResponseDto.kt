package com.jlsh.aifit.feature.diet.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MealItemResponseDto(
    val id: String,
    val name: String,
    val quantity: Float,
    val unit: String,
    val calories: Int,
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatGrams: Float,
)

