package com.jlsh.aifit.feature.nutrition.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TrackFoodItemRequestDto(
    val name: String,
    val quantity: Double? = null,
    val unit: String,
    val calories: Int? = null,
    val proteinGrams: Double? = null,
    val carbsGrams: Double? = null,
    val fatGrams: Double? = null,
    val fiberGrams: Double? = null,
)

