package com.jlsh.aifit.feature.nutrition.domain.util

import com.jlsh.aifit.feature.diet.domain.model.MealType
import com.jlsh.aifit.testutil.fakeFoodItemLog
import com.jlsh.aifit.testutil.fakeFoodPhotoAnalysisResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class FoodPhotoToTrackMealRequestTest {

    @Test
    fun `toTrackMealRequestDto mapea resultado de vision con macros totales`() {
        val result = fakeFoodPhotoAnalysisResult(
            identifiedFoodName = "Ensalada de pollo",
            items = listOf(
                fakeFoodItemLog(
                    name = "Pollo",
                    quantity = 150.0,
                    unit = "g",
                    calories = 250,
                    proteinGrams = 45.0,
                ),
            ),
        )
        val date = LocalDate.of(2026, 6, 3)
        val time = LocalTime.of(13, 30)

        val request = result.toTrackMealRequestDto(date = date, time = time)

        assertEquals("2026-06-03", request.date)
        assertEquals(MealType.LUNCH.name, request.mealType)
        assertEquals("Ensalada de pollo", request.name)
        assertEquals("13:30", request.time)
        assertEquals(1, request.items.size)
        assertEquals("Pollo", request.items.first().name)
        assertEquals(150.0, request.items.first().quantity)
        assertFalse(request.items.first().macrosPer100g)
    }

    @Test
    fun `defaultMealTypeForTime infiere desayuno por la manana`() {
        assertEquals(MealType.BREAKFAST, defaultMealTypeForTime(LocalTime.of(8, 0)))
    }

    @Test
    fun `defaultMealTypeForTime infiere cena por la noche`() {
        assertEquals(MealType.DINNER, defaultMealTypeForTime(LocalTime.of(20, 0)))
    }
}
