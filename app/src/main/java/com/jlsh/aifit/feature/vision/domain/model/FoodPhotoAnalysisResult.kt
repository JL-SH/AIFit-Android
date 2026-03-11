package com.jlsh.aifit.feature.vision.domain.model

import com.jlsh.aifit.feature.nutrition.domain.model.FoodItemLog

data class FoodPhotoAnalysisResult(
    val identifiedFoodName: String,
    val confidence: Double,
    val warnings: List<String>,
    val items: List<FoodItemLog>,
    val rawDescription: String?,
)

