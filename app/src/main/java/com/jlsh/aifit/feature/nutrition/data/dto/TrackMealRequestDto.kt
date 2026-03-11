package com.jlsh.aifit.feature.nutrition.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TrackMealRequestDto(
    val date: String,
    val mealType: String,
    val name: String? = null,
    val time: String,
    val items: List<TrackFoodItemRequestDto>,
)

