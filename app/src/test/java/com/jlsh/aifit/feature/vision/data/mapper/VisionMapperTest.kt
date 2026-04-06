package com.jlsh.aifit.feature.vision.data.mapper

import com.jlsh.aifit.feature.vision.data.mapper.VisionMapper.toDomain
import com.jlsh.aifit.testutil.*
import org.junit.Assert.*
import org.junit.Test

class VisionMapperTest {

    @Test
    fun `toDomain mapea FoodPhotoAnalysisResponseDto correctamente`() {
        val dto = fakeFoodPhotoAnalysisResponseDto()
        val result = dto.toDomain()

        assertEquals("Grilled Chicken Salad", result.identifiedFoodName)
        assertEquals(0.92, result.confidence, 0.001)
        assertTrue(result.warnings.isEmpty())
        assertEquals(1, result.items.size)
        assertEquals("Chicken Breast", result.items[0].name)
        assertEquals(200.0, result.items[0].quantity, 0.001)
        assertEquals("g", result.items[0].unit)
        assertEquals(330, result.items[0].calories)
        assertEquals(62.0, result.items[0].proteinGrams, 0.001)
        assertEquals("A plate of grilled chicken with mixed salad.", result.rawDescription)
    }

    @Test
    fun `toDomain con warnings null produce lista vacia`() {
        val dto = fakeFoodPhotoAnalysisResponseDto(warnings = null)
        val result = dto.toDomain()

        assertTrue(result.warnings.isEmpty())
    }

    @Test
    fun `toDomain con warnings no null los preserva`() {
        val dto = fakeFoodPhotoAnalysisResponseDto(warnings = listOf("Low confidence", "Multiple items"))
        val result = dto.toDomain()

        assertEquals(2, result.warnings.size)
        assertEquals("Low confidence", result.warnings[0])
    }

    @Test
    fun `toDomain con rawDescription null lo preserva`() {
        val dto = fakeFoodPhotoAnalysisResponseDto(rawDescription = null)
        val result = dto.toDomain()

        assertNull(result.rawDescription)
    }

    @Test
    fun `toDomain con items vacios produce lista vacia`() {
        val dto = fakeFoodPhotoAnalysisResponseDto(items = emptyList())
        val result = dto.toDomain()

        assertTrue(result.items.isEmpty())
    }

    @Test
    fun `toDomain mapea multiples items correctamente`() {
        val items = listOf(
            fakeFoodItemLogResponseDto(id = "item-1", name = "Chicken"),
            fakeFoodItemLogResponseDto(id = "item-2", name = "Rice"),
        )
        val dto = fakeFoodPhotoAnalysisResponseDto(items = items)
        val result = dto.toDomain()

        assertEquals(2, result.items.size)
        assertEquals("Chicken", result.items[0].name)
        assertEquals("Rice", result.items[1].name)
    }
}

