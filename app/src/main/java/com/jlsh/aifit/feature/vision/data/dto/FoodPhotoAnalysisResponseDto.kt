package com.jlsh.aifit.feature.vision.data.dto

import com.jlsh.aifit.feature.nutrition.data.dto.FoodItemLogResponseDto
import kotlinx.serialization.Serializable

@Serializable
data class FoodPhotoAnalysisResponseDto(
    val identifiedFoodName: String,
    val confidence: Double,
    val warnings: List<String>? = null,
    val items: List<FoodItemLogResponseDto> = emptyList(),
    val rawDescription: String? = null,
)

