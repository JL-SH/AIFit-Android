package com.jlsh.aifit.feature.nutrition.domain.util

import com.jlsh.aifit.feature.diet.domain.model.MealType
import com.jlsh.aifit.feature.nutrition.data.dto.TrackFoodItemRequestDto
import com.jlsh.aifit.feature.nutrition.data.dto.TrackMealRequestDto
import com.jlsh.aifit.feature.vision.domain.model.FoodPhotoAnalysisResult
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Converts a food photo analysis into a [TrackMealRequestDto] ready to persist.
 */
fun FoodPhotoAnalysisResult.toTrackMealRequestDto(
    date: LocalDate = LocalDate.now(),
    time: LocalTime = LocalTime.now(),
): TrackMealRequestDto {
    val mealType = defaultMealTypeForTime(time)
    val foodItems = items.map { item ->
        TrackFoodItemRequestDto(
            name = item.name,
            quantity = item.quantity,
            unit = item.unit,
            calories = item.calories,
            proteinGrams = item.proteinGrams,
            carbsGrams = item.carbsGrams,
            fatGrams = item.fatGrams,
            macrosPer100g = false,
        )
    }

    return TrackMealRequestDto(
        date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
        mealType = mealType.name,
        name = identifiedFoodName,
        time = time.format(DateTimeFormatter.ofPattern("HH:mm")),
        items = foodItems,
    )
}

/** Infers meal type from clock time when the user logs from a photo scan. */
fun defaultMealTypeForTime(time: LocalTime = LocalTime.now()): MealType = when (time.hour) {
    in 5..9 -> MealType.BREAKFAST
    in 10..11 -> MealType.MID_MORNING
    in 12..15 -> MealType.LUNCH
    in 16..18 -> MealType.AFTERNOON_SNACK
    in 19..22 -> MealType.DINNER
    else -> MealType.AFTERNOON_SNACK
}
