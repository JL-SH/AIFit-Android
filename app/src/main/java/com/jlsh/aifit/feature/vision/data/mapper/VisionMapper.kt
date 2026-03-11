package com.jlsh.aifit.feature.vision.data.mapper

import com.jlsh.aifit.feature.nutrition.domain.model.FoodItemLog
import com.jlsh.aifit.feature.vision.data.dto.FoodPhotoAnalysisResponseDto
import com.jlsh.aifit.feature.vision.domain.model.FoodPhotoAnalysisResult

object VisionMapper {

    fun FoodPhotoAnalysisResponseDto.toDomain(): FoodPhotoAnalysisResult =
        FoodPhotoAnalysisResult(
            identifiedFoodName = identifiedFoodName,
            confidence = confidence,
            warnings = warnings ?: emptyList(),
            items = items.map { dto ->
                FoodItemLog(
                    id = dto.id,
                    name = dto.name,
                    quantity = dto.quantity,
                    unit = dto.unit,
                    calories = dto.calories,
                    proteinGrams = dto.proteinGrams,
                    carbsGrams = dto.carbsGrams,
                    fatGrams = dto.fatGrams,
                )
            },
            rawDescription = rawDescription,
        )
}

