package com.jlsh.aifit.feature.shopping.data.mapper

import com.jlsh.aifit.feature.shopping.data.mapper.ShoppingMapper.toDomain
import com.jlsh.aifit.feature.shopping.data.mapper.ShoppingMapper.toEntity
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingCategory
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingListPeriod
import com.jlsh.aifit.testutil.*
import org.junit.Assert.*
import org.junit.Test

class ShoppingMapperTest {

    // ── ShoppingListResponseDto.toDomain ────────────────────────────────────

    @Test
    fun `toDomain mapea ShoppingListResponseDto correctamente`() {
        val dto = fakeShoppingListResponseDto()
        val result = dto.toDomain()

        assertEquals("slist-1", result.id)
        assertEquals("diet-plan-1", result.dietPlanId)
        assertEquals(ShoppingListPeriod.ONE_WEEK, result.period)
        assertEquals(1, result.categories.size)
        assertEquals(ShoppingCategory.PROTEINS, result.categories[0].category)
        assertEquals("2026-04-06T10:00:00Z", result.generatedAt)
    }

    @Test
    fun `toDomain mapea period desconocido a UNKNOWN`() {
        val dto = fakeShoppingListResponseDto(period = "QUARTERLY")
        val result = dto.toDomain()

        assertEquals(ShoppingListPeriod.UNKNOWN, result.period)
    }

    // ── ShoppingCategoryGroupResponseDto.toDomain ───────────────────────────

    @Test
    fun `toDomain mapea ShoppingCategoryGroupResponseDto correctamente`() {
        val dto = fakeShoppingCategoryGroupResponseDto()
        val result = dto.toDomain()

        assertEquals(ShoppingCategory.PROTEINS, result.category)
        assertEquals(1, result.items.size)
        assertEquals("Chicken Breast", result.items[0].name)
    }

    @Test
    fun `toDomain mapea category desconocida a UNKNOWN`() {
        val dto = fakeShoppingCategoryGroupResponseDto(category = "EXOTIC")
        val result = dto.toDomain()

        assertEquals(ShoppingCategory.UNKNOWN, result.category)
    }

    // ── ShoppingItemResponseDto.toDomain ────────────────────────────────────

    @Test
    fun `toDomain mapea ShoppingItemResponseDto correctamente`() {
        val dto = fakeShoppingItemResponseDto()
        val result = dto.toDomain()

        assertEquals("Chicken Breast", result.name)
        assertEquals(1.5, result.totalQuantity, 0.001)
        assertEquals("kg", result.unit)
        assertEquals("boneless, skinless", result.notes)
        assertFalse(result.isChecked)
    }

    @Test
    fun `toDomain mapea ShoppingItemResponseDto con notes null`() {
        val dto = fakeShoppingItemResponseDto(notes = null)
        val result = dto.toDomain()

        assertNull(result.notes)
    }

    // ── ShoppingListResponseDto.toEntity ────────────────────────────────────

    @Test
    fun `toEntity mapea ShoppingListResponseDto a entidad correctamente`() {
        val dto = fakeShoppingListResponseDto()
        val entity = dto.toEntity()

        assertEquals("slist-1", entity.id)
        assertEquals("diet-plan-1", entity.dietPlanId)
        assertEquals("ONE_WEEK", entity.period)
        assertTrue(entity.generatedAt > 0)
    }

    @Test
    fun `toEntity con fecha invalida asigna 0L`() {
        val dto = fakeShoppingListResponseDto(generatedAt = "not-a-date")
        val entity = dto.toEntity()

        assertEquals(0L, entity.generatedAt)
    }

    // ── ShoppingListEntity.toDomain ─────────────────────────────────────────

    @Test
    fun `toDomain mapea ShoppingListEntity correctamente con categories vacias`() {
        val entity = fakeShoppingListEntity()
        val result = entity.toDomain()

        assertEquals("slist-1", result.id)
        assertEquals("diet-plan-1", result.dietPlanId)
        assertEquals(ShoppingListPeriod.ONE_WEEK, result.period)
        assertTrue(result.categories.isEmpty())
    }

    @Test
    fun `toDomain mapea period desconocido de entidad a UNKNOWN`() {
        val entity = fakeShoppingListEntity(period = "INVALID")
        val result = entity.toDomain()

        assertEquals(ShoppingListPeriod.UNKNOWN, result.period)
    }

    // ── Enum fallback tests ─────────────────────────────────────────────────

    @Test
    fun `ShoppingListPeriod fromString con null retorna UNKNOWN`() {
        assertEquals(ShoppingListPeriod.UNKNOWN, ShoppingListPeriod.fromString(null))
    }

    @Test
    fun `ShoppingListPeriod fromString con valor valido retorna enum correcto`() {
        assertEquals(ShoppingListPeriod.TWO_WEEKS, ShoppingListPeriod.fromString("TWO_WEEKS"))
    }

    @Test
    fun `ShoppingListPeriod fromString con valor desconocido retorna UNKNOWN`() {
        assertEquals(ShoppingListPeriod.UNKNOWN, ShoppingListPeriod.fromString("DAILY"))
    }

    @Test
    fun `ShoppingCategory fromString con null retorna UNKNOWN`() {
        assertEquals(ShoppingCategory.UNKNOWN, ShoppingCategory.fromString(null))
    }

    @Test
    fun `ShoppingCategory fromString con valor valido retorna enum correcto`() {
        assertEquals(ShoppingCategory.DAIRY, ShoppingCategory.fromString("DAIRY"))
    }

    @Test
    fun `ShoppingCategory fromString con valor desconocido retorna UNKNOWN`() {
        assertEquals(ShoppingCategory.UNKNOWN, ShoppingCategory.fromString("EXOTIC_MEATS"))
    }
}

