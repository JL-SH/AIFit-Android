package com.jlsh.aifit.feature.nutrition.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class NutritionMacroCalculationsTest {

    @Test
    fun `scaleCaloriesFromPer100g calcula 150 kcal para 300 kcal por 50 gramos`() {
        assertEquals(150, scaleCaloriesFromPer100g(300, 50.0))
    }

    @Test
    fun `scaleFoodItemMacros escala macros cuando unidad es g`() {
        val scaled = scaleFoodItemMacros(
            unit = "g",
            quantity = 50.0,
            caloriesPer100g = 300,
            proteinPer100g = 20.0,
            carbsPer100g = 10.0,
            fatPer100g = 5.0,
        )
        assertEquals(150, scaled.calories)
        assertEquals(10.0, scaled.proteinGrams!!, 0.001)
        assertEquals(5.0, scaled.carbsGrams!!, 0.001)
        assertEquals(2.5, scaled.fatGrams!!, 0.001)
    }

    @Test
    fun `scaleFoodItemMacros no escala para unidad unit`() {
        val scaled = scaleFoodItemMacros(
            unit = "unit",
            quantity = 2.0,
            caloriesPer100g = 150,
            proteinPer100g = 10.0,
            carbsPer100g = 15.0,
            fatPer100g = 2.0,
        )
        assertEquals(150, scaled.calories)
        assertEquals(10.0, scaled.proteinGrams!!, 0.001)
    }
}
