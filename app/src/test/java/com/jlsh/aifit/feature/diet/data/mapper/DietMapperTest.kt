package com.jlsh.aifit.feature.diet.data.mapper

import com.jlsh.aifit.feature.diet.data.mapper.DietMapper.toDomain
import com.jlsh.aifit.feature.diet.data.mapper.DietMapper.toEntity
import com.jlsh.aifit.feature.diet.domain.model.MealType
import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.user.domain.model.DietPreference
import com.jlsh.aifit.testutil.*
import org.junit.Assert.*
import org.junit.Test

class DietMapperTest {

    // ─── SummaryDto → Domain ───────────────────────────────────────────────────

    @Test
    fun `toDomain convierte summary DTO con todos los campos`() {
        val dto = fakeDietPlanSummaryResponseDto()

        val result = dto.toDomain()

        assertEquals("diet-plan-1", result.id)
        assertEquals("Test Diet", result.name)
        assertEquals(2000, result.dailyCalories)
        assertEquals(150, result.proteinGrams)
        assertEquals(200, result.carbsGrams)
        assertEquals(70, result.fatGrams)
        assertEquals(4, result.durationWeeks)
        assertEquals(DietPreference.NONE, result.preference)
        assertEquals(PlanStatus.ACTIVE, result.status)
        assertEquals(28, result.totalDays)
        assertTrue(result.days.isEmpty())
    }

    // ─── PlanResponseDto → Domain ──────────────────────────────────────────────

    @Test
    fun `toDomain convierte plan DTO con days, meals e items`() {
        val dto = fakeDietPlanResponseDto()

        val result = dto.toDomain()

        assertEquals("diet-plan-1", result.id)
        assertEquals(1, result.days.size)
        assertEquals("Day 1", result.days[0].name)
        assertEquals(1, result.days[0].meals.size)
        assertEquals("Grilled Chicken", result.days[0].meals[0].name)
        assertEquals(1, result.days[0].meals[0].items.size)
        assertEquals("Chicken Breast", result.days[0].meals[0].items[0].name)
    }

    // ─── DayResponseDto → Domain ───────────────────────────────────────────────

    @Test
    fun `toDomain convierte day DTO con meals`() {
        val dto = fakeDietDayResponseDto()

        val result = dto.toDomain()

        assertEquals("dday-1", result.id)
        assertEquals(1, result.dayNumber)
        assertEquals("Day 1", result.name)
        assertEquals(2000, result.totalCalories)
        assertEquals(1, result.meals.size)
    }

    // ─── MealResponseDto → Domain ──────────────────────────────────────────────

    @Test
    fun `toDomain convierte meal DTO con items y mealType`() {
        val dto = fakeMealResponseDto()

        val result = dto.toDomain()

        assertEquals("meal-1", result.id)
        assertEquals(MealType.LUNCH, result.mealType)
        assertEquals("Grilled Chicken", result.name)
        assertEquals("13:00", result.time)
        assertEquals(500, result.calories)
        assertEquals(1, result.items.size)
    }

    // ─── MealItemResponseDto → Domain ──────────────────────────────────────────

    @Test
    fun `toDomain convierte mealItem DTO con campos numéricos`() {
        val dto = fakeMealItemResponseDto()

        val result = dto.toDomain()

        assertEquals("item-1", result.id)
        assertEquals("Chicken Breast", result.name)
        assertEquals(200f, result.quantity, 0.01f)
        assertEquals("g", result.unit)
        assertEquals(330, result.calories)
        assertEquals(62f, result.proteinGrams, 0.01f)
    }

    // ─── Enum fallback UNKNOWN ─────────────────────────────────────────────────

    @Test
    fun `toDomain mapea MealType desconocido a UNKNOWN sin crash`() {
        val dto = fakeMealResponseDto(mealType = "BRUNCH_DELUXE")

        val result = dto.toDomain()

        assertEquals(MealType.UNKNOWN, result.mealType)
    }

    @Test
    fun `toDomain mapea DietPreference desconocido a UNKNOWN sin crash`() {
        val dto = fakeDietPlanSummaryResponseDto().copy(preference = "FRUITARIAN")

        val result = dto.toDomain()

        assertEquals(DietPreference.UNKNOWN, result.preference)
    }

    @Test
    fun `toDomain mapea PlanStatus desconocido a UNKNOWN sin crash`() {
        val dto = fakeDietPlanSummaryResponseDto().copy(status = "INVALID_STATUS")

        val result = dto.toDomain()

        assertEquals(PlanStatus.UNKNOWN, result.status)
    }

    // ─── Domain → Entity ───────────────────────────────────────────────────────

    @Test
    fun `toEntity convierte plan de dominio a entity`() {
        val plan = fakeDietPlan()

        val result = plan.toEntity("user-1")

        assertEquals("diet-plan-1", result.id)
        assertEquals("user-1", result.userId)
        assertEquals("Test Diet", result.name)
        assertEquals("ACTIVE", result.status)
        assertEquals(2000, result.dailyCalories)
        assertEquals(150, result.proteinGrams)
        assertEquals("NONE", result.preference)
    }

    // ─── Entity → Domain ───────────────────────────────────────────────────────

    @Test
    fun `toDomain convierte entity a plan con days vacías`() {
        val entity = fakeDietPlanEntity()

        val result = entity.toDomain()

        assertEquals("diet-plan-1", result.id)
        assertEquals("Test Diet", result.name)
        assertEquals(PlanStatus.ACTIVE, result.status)
        assertEquals(DietPreference.NONE, result.preference)
        assertTrue(result.days.isEmpty())
    }

    @Test
    fun `toDomain de entity con status desconocido mapea a UNKNOWN`() {
        val entity = fakeDietPlanEntity(status = "BANANAS")

        val result = entity.toDomain()

        assertEquals(PlanStatus.UNKNOWN, result.status)
    }

    // ─── Round-trip ────────────────────────────────────────────────────────────

    @Test
    fun `round-trip domain a entity y back preserva campos clave`() {
        val original = fakeDietPlan()

        val entity = original.toEntity("user-1")
        val restored = entity.toDomain()

        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.status, restored.status)
        assertEquals(original.preference, restored.preference)
        assertEquals(original.dailyCalories, restored.dailyCalories)
    }
}

