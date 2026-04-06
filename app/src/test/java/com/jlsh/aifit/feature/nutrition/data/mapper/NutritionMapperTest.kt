package com.jlsh.aifit.feature.nutrition.data.mapper

import com.jlsh.aifit.feature.diet.domain.model.MealType
import com.jlsh.aifit.feature.nutrition.data.mapper.NutritionMapper.toDomain
import com.jlsh.aifit.feature.nutrition.data.mapper.NutritionMapper.toEntity
import com.jlsh.aifit.feature.nutrition.domain.model.TargetSource
import com.jlsh.aifit.testutil.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class NutritionMapperTest {

    // ─── NutritionLogResponseDto.toDomain() ────────────────────────────────────

    @Test
    fun `NutritionLogResponseDto toDomain convierte correctamente todos los campos`() {
        val dto = fakeNutritionLogResponseDto()
        val result = dto.toDomain()

        assertEquals("nutrition-log-1", result.id)
        assertEquals(LocalDate.of(2026, 4, 6), result.date)
        assertEquals(1450, result.totalCalories)
        assertEquals(95.0, result.totalProteinGrams, 0.01)
        assertEquals(180.0, result.totalCarbsGrams, 0.01)
        assertEquals(42.0, result.totalFatGrams, 0.01)
        assertEquals(1, result.meals.size)
    }

    @Test
    fun `NutritionLogResponseDto toDomain con fecha inválida usa fecha actual sin crash`() {
        val dto = fakeNutritionLogResponseDto(date = "INVALID_DATE")
        val result = dto.toDomain()

        assertEquals(LocalDate.now(), result.date)
    }

    // ─── MealLogResponseDto.toDomain() ─────────────────────────────────────────

    @Test
    fun `MealLogResponseDto toDomain convierte correctamente todos los campos`() {
        val dto = fakeMealLogResponseDto()
        val result = dto.toDomain()

        assertEquals("meal-log-1", result.id)
        assertEquals(MealType.LUNCH, result.mealType)
        assertEquals("Grilled Chicken", result.name)
        assertEquals("13:00", result.time)
        assertEquals(520, result.calories)
        assertEquals(45.0, result.proteinGrams, 0.01)
        assertEquals(30.0, result.carbsGrams, 0.01)
        assertEquals(18.0, result.fatGrams, 0.01)
        assertFalse(result.aiGenerated)
        assertNull(result.rawInputText)
        assertEquals(1, result.items.size)
    }

    @Test
    fun `MealLogResponseDto toDomain con mealType desconocido mapea a UNKNOWN`() {
        val dto = fakeMealLogResponseDto(mealType = "BRUNCH_ESPECIAL")
        val result = dto.toDomain()

        assertEquals(MealType.UNKNOWN, result.mealType)
    }

    // ─── FoodItemLogResponseDto.toDomain() ─────────────────────────────────────

    @Test
    fun `FoodItemLogResponseDto toDomain convierte correctamente todos los campos`() {
        val dto = fakeFoodItemLogResponseDto()
        val result = dto.toDomain()

        assertEquals("food-item-1", result.id)
        assertEquals("Chicken Breast", result.name)
        assertEquals(200.0, result.quantity, 0.01)
        assertEquals("g", result.unit)
        assertEquals(330, result.calories)
        assertEquals(62.0, result.proteinGrams, 0.01)
        assertEquals(0.0, result.carbsGrams, 0.01)
        assertEquals(7.0, result.fatGrams, 0.01)
    }

    // ─── NutritionTargetResponseDto.toDomain() ────────────────────────────────

    @Test
    fun `NutritionTargetResponseDto toDomain convierte correctamente todos los campos`() {
        val dto = fakeNutritionTargetResponseDto()
        val result = dto.toDomain()

        assertEquals("target-1", result.id)
        assertEquals(2200, result.calorieTarget)
        assertEquals(165.0, result.proteinTarget, 0.01)
        assertEquals(250.0, result.carbsTarget, 0.01)
        assertEquals(73.0, result.fatTarget, 0.01)
        assertEquals(LocalDate.of(2026, 4, 1), result.effectiveFrom)
        assertEquals(TargetSource.MANUAL, result.setBy)
    }

    @Test
    fun `NutritionTargetResponseDto toDomain con setBy desconocido mapea a UNKNOWN`() {
        val dto = fakeNutritionTargetResponseDto(setBy = "COSMIC_AI")
        val result = dto.toDomain()

        assertEquals(TargetSource.UNKNOWN, result.setBy)
    }

    // ─── NutritionLog.toEntity() ───────────────────────────────────────────────

    @Test
    fun `NutritionLog toEntity convierte correctamente a entidad Room`() {
        val log = fakeNutritionLog()
        val entity = log.toEntity()

        assertEquals("nutrition-log-1", entity.id)
        assertEquals(LocalDate.of(2026, 4, 6).toEpochDay(), entity.date)
        assertEquals(1450, entity.totalCalories)
        assertEquals(95.0, entity.totalProteinGrams, 0.01)
        assertEquals(180.0, entity.totalCarbsGrams, 0.01)
        assertEquals(42.0, entity.totalFatGrams, 0.01)
    }

    // ─── NutritionLogEntity.toDomain() ─────────────────────────────────────────

    @Test
    fun `NutritionLogEntity toDomain convierte correctamente sin meals`() {
        val entity = fakeNutritionLogEntity()
        val result = entity.toDomain()

        assertEquals("nutrition-log-1", result.id)
        assertEquals(LocalDate.of(2026, 4, 6), result.date)
        assertEquals(1450, result.totalCalories)
        assertEquals(95.0, result.totalProteinGrams, 0.01)
        assertEquals(180.0, result.totalCarbsGrams, 0.01)
        assertEquals(42.0, result.totalFatGrams, 0.01)
        assertTrue(result.meals.isEmpty())
    }
}

