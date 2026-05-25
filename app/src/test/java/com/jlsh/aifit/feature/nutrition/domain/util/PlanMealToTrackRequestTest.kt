package com.jlsh.aifit.feature.nutrition.domain.util

import com.jlsh.aifit.feature.diet.domain.model.MealType
import com.jlsh.aifit.testutil.fakeMeal
import com.jlsh.aifit.testutil.fakeMealItem
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class PlanMealToTrackRequestTest {

    @Test
    fun `toTrackMealRequestDto mapea comida con items del plan`() {
        val meal = fakeMeal(
            id = "meal-1",
            mealType = MealType.LUNCH,
            name = "Pollo con arroz",
            calories = 650,
            items = listOf(
                fakeMealItem(
                    id = "item-1",
                    name = "Pollo",
                    quantity = 150f,
                    unit = "g",
                    calories = 250,
                ),
            ),
        )

        val request = meal.toTrackMealRequestDto(LocalDate.of(2026, 5, 25))

        assertEquals("2026-05-25", request.date)
        assertEquals(MealType.LUNCH.name, request.mealType)
        assertEquals("Pollo con arroz", request.name)
        assertEquals("13:00", request.time)
        assertEquals(1, request.items.size)
        assertEquals("Pollo", request.items.first().name)
        assertEquals(150.0, request.items.first().quantity)
        assertEquals(false, request.items.first().macrosPer100g)
    }

    @Test
    fun `toTrackMealRequestDto usa resumen de comida cuando no hay items`() {
        val meal = fakeMeal(
            name = "Batido proteico",
            calories = 320,
            items = emptyList(),
        ).copy(
            time = "",
            proteinGrams = 30,
            carbsGrams = 20,
            fatGrams = 8,
        )

        val request = meal.toTrackMealRequestDto()

        assertEquals(1, request.items.size)
        assertEquals("Batido proteico", request.items.first().name)
        assertEquals(320, request.items.first().calories)
    }
}
