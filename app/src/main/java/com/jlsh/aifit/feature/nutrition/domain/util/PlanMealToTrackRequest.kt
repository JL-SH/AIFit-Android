package com.jlsh.aifit.feature.nutrition.domain.util

import com.jlsh.aifit.feature.diet.domain.model.Meal
import com.jlsh.aifit.feature.diet.domain.model.MealType
import com.jlsh.aifit.feature.nutrition.data.dto.TrackFoodItemRequestDto
import com.jlsh.aifit.feature.nutrition.data.dto.TrackMealRequestDto
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Convert a diet plan meal into a [TrackMealRequestDto] ready to record.
 */
fun Meal.toTrackMealRequestDto(date: LocalDate = LocalDate.now()): TrackMealRequestDto {
    val resolvedTime = time.trim().let { raw ->
        if (raw.isNotBlank()) {
            try {
                LocalTime.parse(
                    if (raw.length == 4 && raw[1] == ':') "0$raw" else raw,
                )
                raw
            } catch (_: Exception) {
                estimatedTimeForMealType(mealType)
            }
        } else {
            estimatedTimeForMealType(mealType)
        }
    }

    val foodItems = if (items.isNotEmpty()) {
        items.map { item ->
            TrackFoodItemRequestDto(
                name = item.name,
                quantity = item.quantity.toDouble(),
                unit = item.unit,
                calories = item.calories,
                proteinGrams = item.proteinGrams.toDouble(),
                carbsGrams = item.carbsGrams.toDouble(),
                fatGrams = item.fatGrams.toDouble(),
                macrosPer100g = false,
            )
        }
    } else {
        listOf(
            TrackFoodItemRequestDto(
                name = name,
                quantity = 1.0,
                unit = "unit",
                calories = calories,
                proteinGrams = proteinGrams.toDouble(),
                carbsGrams = carbsGrams.toDouble(),
                fatGrams = fatGrams.toDouble(),
                macrosPer100g = false,
            ),
        )
    }

    return TrackMealRequestDto(
        date = date.toString(),
        mealType = mealType.name,
        name = name,
        time = resolvedTime,
        items = foodItems,
    )
}

private fun estimatedTimeForMealType(type: MealType): String = when (type) {
    MealType.BREAKFAST -> "08:00"
    MealType.MID_MORNING -> "10:30"
    MealType.LUNCH -> "13:00"
    MealType.AFTERNOON_SNACK -> "16:30"
    MealType.DINNER -> "20:00"
    MealType.PRE_WORKOUT -> "17:00"
    MealType.POST_WORKOUT -> "19:00"
    MealType.UNKNOWN -> "12:00"
}
