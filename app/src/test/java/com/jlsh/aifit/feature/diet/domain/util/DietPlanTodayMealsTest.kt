package com.jlsh.aifit.feature.diet.domain.util

import com.jlsh.aifit.testutil.fakeDietDay
import com.jlsh.aifit.testutil.fakeDietPlan
import com.jlsh.aifit.testutil.fakeMeal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DietPlanTodayMealsTest {

    @Test
    fun `mealsForToday devuelve comidas del dia segun dayOfWeek`() {
        val dayOfWeek = LocalDate.now().dayOfWeek.value
        val targetIndex = (dayOfWeek - 1) % 3

        val breakfast = fakeMeal(id = "m-breakfast", name = "Desayuno plan")
        val lunch = fakeMeal(id = "m-lunch", name = "Comida plan")
        val plan = fakeDietPlan(
            days = listOf(
                fakeDietDay(id = "d1", dayNumber = 1, meals = listOf(breakfast)),
                fakeDietDay(id = "d2", dayNumber = 2, meals = listOf(lunch)),
                fakeDietDay(id = "d3", dayNumber = 3, meals = emptyList()),
            ),
        )

        val meals = plan.mealsForToday()
        val expected = when (targetIndex) {
            0 -> listOf(breakfast)
            1 -> listOf(lunch)
            else -> emptyList()
        }
        assertEquals(expected, meals)
    }

    @Test
    fun `mealsForToday devuelve vacio sin dias`() {
        val plan = fakeDietPlan(days = emptyList())
        assertTrue(plan.mealsForToday().isEmpty())
    }
}
