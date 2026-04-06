package com.jlsh.aifit.feature.progression.data.mapper

import com.jlsh.aifit.feature.progression.data.mapper.ProgressionMapper.toDomain
import com.jlsh.aifit.feature.progression.domain.model.ProgressTrend
import com.jlsh.aifit.feature.progression.domain.model.ProgressionType
import com.jlsh.aifit.testutil.*
import org.junit.Assert.*
import org.junit.Test

class ProgressionMapperTest {

    // ─── ProgressionRecommendationResponseDto.toDomain() ───────────────────────

    @Test
    fun `ProgressionRecommendationResponseDto toDomain convierte correctamente todos los campos`() {
        val dto = fakeProgressionRecommendationResponseDto()
        val result = dto.toDomain()

        assertEquals("exercise-1", result.trainingExerciseId)
        assertEquals("Bench Press", result.exerciseName)
        assertEquals(ProgressionType.INCREASE_LOAD, result.type)
        assertEquals(60.0, result.currentLoad!!, 0.01)
        assertEquals(65.0, result.suggestedLoad!!, 0.01)
        assertEquals(8, result.suggestedRepsMin)
        assertEquals(10, result.suggestedRepsMax)
        assertEquals("Consistent performance in last 3 sessions allows a load increase.", result.rationale)
        assertEquals(0.85, result.confidence, 0.01)
        assertEquals(3, result.basedOnSessions)
    }

    @Test
    fun `ProgressionRecommendationResponseDto toDomain con type desconocido mapea a UNKNOWN`() {
        val dto = fakeProgressionRecommendationResponseDto(type = "SUPER_SPECIAL_LOAD")
        val result = dto.toDomain()

        assertEquals(ProgressionType.UNKNOWN, result.type)
    }

    @Test
    fun `ProgressionType fromString con null mapea a UNKNOWN`() {
        assertEquals(ProgressionType.UNKNOWN, ProgressionType.fromString(null))
    }

    @Test
    fun `ProgressionType fromString con valores válidos mapea correctamente`() {
        assertEquals(ProgressionType.MAINTAIN_LOAD, ProgressionType.fromString("MAINTAIN_LOAD"))
        assertEquals(ProgressionType.DECREASE_REPS, ProgressionType.fromString("DECREASE_REPS"))
        assertEquals(ProgressionType.DELOAD, ProgressionType.fromString("DELOAD"))
        assertEquals(ProgressionType.CHANGE_REP_RANGE, ProgressionType.fromString("CHANGE_REP_RANGE"))
        assertEquals(ProgressionType.INSUFFICIENT_DATA, ProgressionType.fromString("INSUFFICIENT_DATA"))
    }

    // ─── PlanProgressionSummaryResponseDto.toDomain() ──────────────────────────

    @Test
    fun `PlanProgressionSummaryResponseDto toDomain mapea trainingPlanId a planId`() {
        val dto = fakePlanProgressionSummaryResponseDto(trainingPlanId = "plan-42")
        val result = dto.toDomain()

        assertEquals("plan-42", result.planId)
        assertEquals(1, result.recommendations.size)
        // overallTrend is hardcoded to UNKNOWN in mapper
        assertEquals(ProgressTrend.UNKNOWN, result.overallTrend)
    }

    @Test
    fun `PlanProgressionSummaryResponseDto toDomain con lista vacía de recommendations`() {
        val dto = fakePlanProgressionSummaryResponseDto(recommendations = emptyList())
        val result = dto.toDomain()

        assertTrue(result.recommendations.isEmpty())
    }

    // ─── ProgressTrend enum ────────────────────────────────────────────────────

    @Test
    fun `ProgressTrend fromString con null mapea a UNKNOWN`() {
        assertEquals(ProgressTrend.UNKNOWN, ProgressTrend.fromString(null))
    }

    @Test
    fun `ProgressTrend fromString con valor desconocido mapea a UNKNOWN`() {
        assertEquals(ProgressTrend.UNKNOWN, ProgressTrend.fromString("SKYROCKETING"))
    }

    @Test
    fun `ProgressTrend fromString con valores válidos mapea correctamente`() {
        assertEquals(ProgressTrend.IMPROVING, ProgressTrend.fromString("IMPROVING"))
        assertEquals(ProgressTrend.MAINTAINING, ProgressTrend.fromString("MAINTAINING"))
        assertEquals(ProgressTrend.DECLINING, ProgressTrend.fromString("DECLINING"))
        assertEquals(ProgressTrend.INSUFFICIENT_DATA, ProgressTrend.fromString("INSUFFICIENT_DATA"))
    }
}

