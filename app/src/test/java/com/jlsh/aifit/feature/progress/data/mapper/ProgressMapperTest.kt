package com.jlsh.aifit.feature.progress.data.mapper

import com.jlsh.aifit.feature.progress.data.dto.BestSetResponseDto
import com.jlsh.aifit.feature.progress.data.mapper.ProgressMapper.toDomain
import com.jlsh.aifit.feature.progress.data.mapper.ProgressMapper.toEntity
import com.jlsh.aifit.feature.progress.domain.model.WeightTrend
import com.jlsh.aifit.testutil.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ProgressMapperTest {

    // ─── ProgressDashboardResponseDto.toDomain() ───────────────────────────────

    @Test
    fun `ProgressDashboardResponseDto toDomain convierte correctamente todos los campos`() {
        val dto = fakeProgressDashboardResponseDto()
        val result = dto.toDomain()

        assertEquals(LocalDate.of(2026, 3, 1), result.periodFrom)
        assertEquals(LocalDate.of(2026, 3, 31), result.periodTo)
        assertEquals(12, result.workoutAdherence.plannedSessions)
        assertEquals(10, result.workoutAdherence.completedSessions)
        assertEquals(80.0, result.weightProgress.startWeight, 0.01)
        assertEquals(78.5, result.weightProgress.currentWeight, 0.01)
        assertEquals(2100.0, result.nutritionAdherence.averageCalories, 0.01)
        assertEquals(1, result.strengthProgress.size)
    }

    // ─── WorkoutAdherenceResponseDto.toDomain() ────────────────────────────────

    @Test
    fun `WorkoutAdherenceResponseDto toDomain convierte correctamente todos los campos`() {
        val dto = fakeWorkoutAdherenceResponseDto()
        val result = dto.toDomain()

        assertEquals(12, result.plannedSessions)
        assertEquals(10, result.completedSessions)
        assertEquals(83.3, result.adherencePercentage, 0.01)
        assertEquals(5, result.currentStreak)
        assertEquals(8, result.longestStreak)
    }

    // ─── WeightProgressResponseDto.toDomain() ──────────────────────────────────

    @Test
    fun `WeightProgressResponseDto toDomain mapea initialWeight a startWeight`() {
        val dto = fakeWeightProgressResponseDto(initialWeight = 85.0)
        val result = dto.toDomain()

        assertEquals(85.0, result.startWeight, 0.01)
        assertEquals(78.5, result.currentWeight, 0.01)
        assertEquals(75.0, result.targetWeight, 0.01)
        assertEquals(-1.5, result.change, 0.01)
        assertEquals(WeightTrend.LOSING, result.trend)
        assertEquals(2, result.entries.size)
    }

    @Test
    fun `WeightProgressResponseDto toDomain con trend desconocido mapea a UNKNOWN`() {
        val dto = fakeWeightProgressResponseDto(trend = "ZIGZAG_TREND")
        val result = dto.toDomain()

        assertEquals(WeightTrend.UNKNOWN, result.trend)
    }

    @Test
    fun `WeightTrend fromString con null mapea a UNKNOWN`() {
        assertEquals(WeightTrend.UNKNOWN, WeightTrend.fromString(null))
    }

    // ─── NutritionAdherenceResponseDto.toDomain() ──────────────────────────────

    @Test
    fun `NutritionAdherenceResponseDto toDomain mapea nombres de campos correctamente`() {
        val dto = fakeNutritionAdherenceResponseDto()
        val result = dto.toDomain()

        assertEquals(2100.0, result.averageCalories, 0.01)
        assertEquals(2200, result.calorieTarget)
        assertEquals(95.5, result.adherencePercentage, 0.01)
    }

    // ─── StrengthProgressResponseDto.toDomain() ────────────────────────────────

    @Test
    fun `StrengthProgressResponseDto toDomain mapea bestSet weights a startMax y currentMax`() {
        val dto = fakeStrengthProgressResponseDto(
            bestSetStart = BestSetResponseDto(date = "2026-03-01", reps = 8, weight = 50.0),
            bestSetEnd = BestSetResponseDto(date = "2026-03-30", reps = 8, weight = 65.0),
            progressionPercentage = 30.0,
        )
        val result = dto.toDomain()

        assertEquals("Bench Press", result.exerciseName)
        assertEquals(50.0, result.startMax, 0.01)
        assertEquals(65.0, result.currentMax, 0.01)
        assertEquals(30.0, result.changePercentage, 0.01)
    }

    // ─── WeeklyProgressSummaryResponseDto.toDomain() ───────────────────────────

    @Test
    fun `WeeklyProgressSummaryResponseDto toDomain convierte correctamente todos los campos`() {
        val dto = fakeWeeklyProgressSummaryResponseDto()
        val result = dto.toDomain()

        assertEquals(3, result.workoutsThisWeek)
        assertEquals(4, result.workoutsTarget)
        assertEquals(2050.0, result.averageCaloriesToday, 0.01)
        assertEquals(2200, result.calorieTarget)
        assertEquals(5, result.currentStreak)
        assertEquals(78.5, result.bodyWeight!!, 0.01)
    }

    // ─── BodyWeightLogResponseDto.toDomain() ───────────────────────────────────

    @Test
    fun `BodyWeightLogResponseDto toDomain convierte correctamente todos los campos`() {
        val dto = fakeBodyWeightLogResponseDto()
        val result = dto.toDomain()

        assertEquals("bw-1", result.id)
        assertEquals(78.5, result.weight, 0.01)
        assertEquals(LocalDate.of(2026, 3, 15), result.date)
        assertEquals("Morning weight", result.notes)
        assertEquals(LocalDate.of(2026, 3, 15), result.createdAt)
    }

    @Test
    fun `BodyWeightLogResponseDto toDomain con fecha inválida usa fecha actual sin crash`() {
        val dto = fakeBodyWeightLogResponseDto(date = "NOT_A_DATE")
        val result = dto.toDomain()

        assertEquals(LocalDate.now(), result.date)
    }

    // ─── BodyWeightLog.toEntity() ──────────────────────────────────────────────

    @Test
    fun `BodyWeightLog toEntity convierte dates a epochDay`() {
        val domain = fakeBodyWeightLog()
        val entity = domain.toEntity()

        assertEquals("bw-1", entity.id)
        assertEquals(78.5, entity.weight, 0.01)
        assertEquals(LocalDate.of(2026, 3, 15).toEpochDay(), entity.date)
        assertEquals("Morning weight", entity.notes)
        assertEquals(LocalDate.of(2026, 3, 15).toEpochDay(), entity.createdAt)
    }

    // ─── BodyWeightEntity.toDomain() ───────────────────────────────────────────

    @Test
    fun `BodyWeightEntity toDomain convierte epochDay a LocalDate`() {
        val entity = fakeBodyWeightEntity()
        val domain = entity.toDomain()

        assertEquals("bw-1", domain.id)
        assertEquals(78.5, domain.weight, 0.01)
        assertEquals(LocalDate.of(2026, 3, 15), domain.date)
        assertEquals("Morning weight", domain.notes)
        assertEquals(LocalDate.of(2026, 3, 15), domain.createdAt)
    }
}

