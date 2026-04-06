package com.jlsh.aifit.feature.metabolic.data.mapper

import com.jlsh.aifit.feature.metabolic.data.mapper.MetabolicMapper.toDomain
import com.jlsh.aifit.feature.metabolic.domain.model.AdjustmentMagnitude
import com.jlsh.aifit.feature.metabolic.domain.model.AdjustmentType
import com.jlsh.aifit.feature.metabolic.domain.model.AdjustmentUrgency
import com.jlsh.aifit.feature.metabolic.domain.model.MetabolicStatus
import com.jlsh.aifit.testutil.*
import org.junit.Assert.*
import org.junit.Test

class MetabolicMapperTest {

    // ── MetabolicAnalysisResponseDto.toDomain ───────────────────────────────

    @Test
    fun `toDomain mapea MetabolicAnalysisResponseDto correctamente`() {
        val dto = fakeMetabolicAnalysisResponseDto()
        val result = dto.toDomain()

        assertEquals(MetabolicStatus.ON_TRACK, result.status)
        assertEquals(-0.3, result.weightTrend.averageWeeklyChange, 0.001)
        assertEquals("LOSING", result.weightTrend.trend)
        assertEquals(-0.5, result.weightTrend.expectedWeeklyChange, 0.001)
        assertEquals(0.2, result.weightTrend.deviationFromExpected, 0.001)
        assertEquals(14, result.weightTrend.dataPoints)
        assertEquals(92.5, result.calorieAdherenceRate, 0.001)
        assertEquals(-150.0, result.averageCalorieDeficitSurplus, 0.001)
        assertNotNull(result.recommendation)
        assertEquals(AdjustmentType.DECREASE_CALORIES, result.recommendation?.type)
        assertEquals("Your progress is on track.", result.rationale)
    }

    @Test
    fun `toDomain mapea MetabolicAnalysisResponseDto sin recomendacion`() {
        val dto = fakeMetabolicAnalysisResponseDto(recommendation = null)
        val result = dto.toDomain()

        assertNull(result.recommendation)
    }

    @Test
    fun `toDomain mapea status desconocido a UNKNOWN en MetabolicAnalysis`() {
        val dto = fakeMetabolicAnalysisResponseDto(status = "INVALID_STATUS")
        val result = dto.toDomain()

        assertEquals(MetabolicStatus.UNKNOWN, result.status)
    }

    // ── WeightTrendResponseDto.toDomain ─────────────────────────────────────

    @Test
    fun `toDomain mapea WeightTrendResponseDto correctamente`() {
        val dto = fakeWeightTrendResponseDto()
        val result = dto.toDomain()

        assertEquals(-0.3, result.averageWeeklyChange, 0.001)
        assertEquals("LOSING", result.trend)
        assertEquals(-0.5, result.expectedWeeklyChange, 0.001)
        assertEquals(0.2, result.deviationFromExpected, 0.001)
        assertEquals(14, result.dataPoints)
    }

    // ── MetabolicAdjustmentRecommendationResponseDto.toDomain ───────────────

    @Test
    fun `toDomain mapea MetabolicAdjustmentRecommendationResponseDto correctamente`() {
        val dto = fakeMetabolicAdjustmentRecommendationResponseDto()
        val result = dto.toDomain()

        assertEquals(AdjustmentType.DECREASE_CALORIES, result.type)
        assertEquals(2000, result.suggestedCalorieTarget)
        assertEquals(160.0, result.suggestedProteinTarget, 0.001)
        assertEquals(220.0, result.suggestedCarbsTarget, 0.001)
        assertEquals(65.0, result.suggestedFatTarget, 0.001)
        assertEquals(AdjustmentMagnitude.MODERATE, result.magnitude)
        assertEquals(AdjustmentUrgency.SUGGESTED, result.urgency)
    }

    @Test
    fun `toDomain mapea type desconocido a UNKNOWN en AdjustmentRecommendation`() {
        val dto = fakeMetabolicAdjustmentRecommendationResponseDto(type = "WEIRD_TYPE")
        val result = dto.toDomain()

        assertEquals(AdjustmentType.UNKNOWN, result.type)
    }

    @Test
    fun `toDomain mapea magnitude desconocida a UNKNOWN en AdjustmentRecommendation`() {
        val dto = fakeMetabolicAdjustmentRecommendationResponseDto(magnitude = "HUGE")
        val result = dto.toDomain()

        assertEquals(AdjustmentMagnitude.UNKNOWN, result.magnitude)
    }

    @Test
    fun `toDomain mapea urgency desconocida a UNKNOWN en AdjustmentRecommendation`() {
        val dto = fakeMetabolicAdjustmentRecommendationResponseDto(urgency = "CRITICAL")
        val result = dto.toDomain()

        assertEquals(AdjustmentUrgency.UNKNOWN, result.urgency)
    }

    // ── MetabolicInsightResponseDto.toDomain ────────────────────────────────

    @Test
    fun `toDomain mapea MetabolicInsightResponseDto correctamente`() {
        val dto = fakeMetabolicInsightResponseDto()
        val result = dto.toDomain()

        assertEquals("insight-1", result.id)
        assertEquals(MetabolicStatus.ON_TRACK, result.statusAtTime)
        assertEquals(AdjustmentType.DECREASE_CALORIES, result.adjustmentType)
        assertEquals(2200, result.previousCalorieTarget)
        assertEquals(2000, result.newCalorieTarget)
        assertEquals(AdjustmentMagnitude.MODERATE, result.magnitude)
        assertEquals("Adherence high, reducing surplus.", result.rationale)
        assertEquals("2026-04-01T10:00:00Z", result.appliedAt)
    }

    @Test
    fun `toDomain mapea statusAtTime desconocido a UNKNOWN en MetabolicInsight`() {
        val dto = fakeMetabolicInsightResponseDto(statusAtTime = "BAD_STATUS")
        val result = dto.toDomain()

        assertEquals(MetabolicStatus.UNKNOWN, result.statusAtTime)
    }

    @Test
    fun `toDomain mapea adjustmentType desconocido a UNKNOWN en MetabolicInsight`() {
        val dto = fakeMetabolicInsightResponseDto(adjustmentType = "INVALID")
        val result = dto.toDomain()

        assertEquals(AdjustmentType.UNKNOWN, result.adjustmentType)
    }

    // ── Enum fallback tests ─────────────────────────────────────────────────

    @Test
    fun `MetabolicStatus fromString con null retorna UNKNOWN`() {
        assertEquals(MetabolicStatus.UNKNOWN, MetabolicStatus.fromString(null))
    }

    @Test
    fun `MetabolicStatus fromString con valor valido retorna enum correcto`() {
        assertEquals(MetabolicStatus.STAGNATED, MetabolicStatus.fromString("STAGNATED"))
    }

    @Test
    fun `AdjustmentType fromString con null retorna UNKNOWN`() {
        assertEquals(AdjustmentType.UNKNOWN, AdjustmentType.fromString(null))
    }

    @Test
    fun `AdjustmentType fromString con valor valido retorna enum correcto`() {
        assertEquals(AdjustmentType.INCREASE_PROTEIN, AdjustmentType.fromString("INCREASE_PROTEIN"))
    }

    @Test
    fun `AdjustmentMagnitude fromString con null retorna UNKNOWN`() {
        assertEquals(AdjustmentMagnitude.UNKNOWN, AdjustmentMagnitude.fromString(null))
    }

    @Test
    fun `AdjustmentMagnitude fromString con valor valido retorna enum correcto`() {
        assertEquals(AdjustmentMagnitude.SIGNIFICANT, AdjustmentMagnitude.fromString("SIGNIFICANT"))
    }

    @Test
    fun `AdjustmentUrgency fromString con null retorna UNKNOWN`() {
        assertEquals(AdjustmentUrgency.UNKNOWN, AdjustmentUrgency.fromString(null))
    }

    @Test
    fun `AdjustmentUrgency fromString con valor valido retorna enum correcto`() {
        assertEquals(AdjustmentUrgency.RECOMMENDED, AdjustmentUrgency.fromString("RECOMMENDED"))
    }
}

