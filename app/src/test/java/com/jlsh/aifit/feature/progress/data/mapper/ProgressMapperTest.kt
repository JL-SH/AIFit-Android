package com.jlsh.aifit.feature.progress.data.mapper

import com.jlsh.aifit.feature.progress.data.dto.BestSetResponseDto
import com.jlsh.aifit.feature.progress.data.dto.ProgressDashboardResponseDto
import com.jlsh.aifit.feature.progress.data.dto.StrengthProgressResponseDto
import com.jlsh.aifit.feature.progress.data.dto.WeeklyProgressSummaryResponseDto
import com.jlsh.aifit.feature.progress.data.mapper.ProgressMapper.toDomain
import com.jlsh.aifit.feature.progress.data.mapper.ProgressMapper.toEntity
import com.jlsh.aifit.feature.progress.domain.model.WeightTrend
import com.jlsh.aifit.testutil.*
import kotlinx.serialization.json.Json
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
        assertEquals(80.0, result.weightProgress.startWeight!!, 0.01)
        assertEquals(78.5, result.weightProgress.currentWeight!!, 0.01)
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

        assertEquals(85.0, result.startWeight!!, 0.01)
        assertEquals(78.5, result.currentWeight!!, 0.01)
        assertEquals(75.0, result.targetWeight!!, 0.01)
        assertEquals(-1.5, result.change!!, 0.01)
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
        val result = requireNotNull(dto.toDomain())

        assertEquals("Bench Press", result.exerciseName)
        assertEquals(50.0, result.startMax, 0.01)
        assertEquals(65.0, result.currentMax, 0.01)
        assertEquals(30.0, result.changePercentage, 0.01)
    }

    @Test
    fun `StrengthProgressResponseDto toDomain retorna null cuando weight es null`() {
        val dto = fakeStrengthProgressResponseDto(
            bestSetStart = BestSetResponseDto(date = "2026-01-01", reps = 12, weight = null),
            bestSetEnd = BestSetResponseDto(date = "2026-01-15", reps = 12, weight = null),
        )

        assertNull(dto.toDomain())
    }

    @Test
    fun `StrengthProgressResponseDto toDomain retorna null cuando bestSetStart o bestSetEnd son null`() {
        val dto = StrengthProgressResponseDto(
            exerciseName = "Pull-ups",
            trainingExerciseId = "exercise-2",
            bestSetStart = null,
            bestSetEnd = null,
            progressionPercentage = null,
            trend = "INSUFFICIENT_DATA",
        )

        assertNull(dto.toDomain())
    }

    @Test
    fun `ProgressDashboardResponseDto toDomain omite strengthProgress sin peso comparable`() {
        val dto = fakeProgressDashboardResponseDto(
            strengthProgress = listOf(
                fakeStrengthProgressResponseDto(),
                fakeStrengthProgressResponseDto(
                    exerciseName = "Pull-ups",
                    bestSetStart = BestSetResponseDto(date = "2026-01-01", reps = 10, weight = null),
                    bestSetEnd = BestSetResponseDto(date = "2026-01-15", reps = 12, weight = null),
                ),
            ),
        )

        val result = dto.toDomain()

        assertEquals(1, result.strengthProgress.size)
        assertEquals("Bench Press", result.strengthProgress.first().exerciseName)
    }

    @Test
    fun `deserializa strengthProgress con weight null sin lanzar excepción`() {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }
        val payload = """
            {
              "period": {"from": "2026-03-01", "to": "2026-03-31"},
              "workoutAdherence": {
                "plannedSessions": 12,
                "completedSessions": 10,
                "adherencePercentage": 83.3,
                "currentStreak": 5,
                "longestStreak": 8
              },
              "weightProgress": {
                "initialWeight": 80.0,
                "currentWeight": 78.5,
                "targetWeight": 75.0,
                "change": -1.5,
                "trend": "LOSING",
                "entries": []
              },
              "nutritionAdherence": {
                "targetCalories": 2200,
                "averageCaloriesConsumed": 2100.0,
                "calorieAdherencePercentage": 95.5,
                "targetProtein": 150.0,
                "averageProteinConsumed": 140.0,
                "proteinAdherencePercentage": 93.3,
                "daysTracked": 20
              },
              "strengthProgress": [{
                "exerciseName": "Pull-ups",
                "trainingExerciseId": "exercise-2",
                "bestSetStart": {"date": "2026-01-01", "reps": 12, "weight": null},
                "bestSetEnd": {"date": "2026-01-15", "reps": 12, "weight": null},
                "progressionPercentage": null,
                "trend": "INSUFFICIENT_DATA"
              }],
              "generatedAt": "2026-03-31T23:59:59"
            }
        """.trimIndent()

        val dto = json.decodeFromString<ProgressDashboardResponseDto>(payload)
        val result = dto.toDomain()

        assertTrue(result.strengthProgress.isEmpty())
    }

    // ─── WeeklyProgressSummaryResponseDto.toDomain() ───────────────────────────

    @Test
    fun `WeeklyProgressSummaryResponseDto toDomain convierte correctamente todos los campos`() {
        val dto = fakeWeeklyProgressSummaryResponseDto()
        val result = dto.toDomain()

        assertEquals(3, result.workoutsThisWeek)
        assertEquals(4, result.workoutsTarget)
        assertEquals(2050.0, result.averageCaloriesToday!!, 0.01)
        assertEquals(2200, result.calorieTarget)
        assertEquals(5, result.currentStreak)
        assertEquals(78.5, result.bodyWeight!!, 0.01)
    }

    @Test
    fun `WeeklyProgressSummaryResponseDto toDomain mapea averageCaloriesToday null`() {
        val dto = fakeWeeklyProgressSummaryResponseDto(averageCaloriesToday = null)
        val result = dto.toDomain()

        assertNull(result.averageCaloriesToday)
    }

    @Test
    fun `deserializa WeeklyProgressSummaryResponseDto con averageCaloriesToday null`() {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }
        val payload = """
            {
              "workoutsThisWeek": 3,
              "workoutsTarget": 4,
              "averageCaloriesToday": null,
              "calorieTarget": 2200,
              "currentStreak": 5,
              "bodyWeight": 78.5
            }
        """.trimIndent()

        val dto = json.decodeFromString<WeeklyProgressSummaryResponseDto>(payload)
        val result = dto.toDomain()

        assertNull(result.averageCaloriesToday)
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

