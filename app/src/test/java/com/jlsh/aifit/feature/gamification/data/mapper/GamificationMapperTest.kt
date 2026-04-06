package com.jlsh.aifit.feature.gamification.data.mapper

import com.jlsh.aifit.feature.gamification.data.mapper.GamificationMapper.toDomain
import com.jlsh.aifit.feature.gamification.domain.model.AchievementRarity
import com.jlsh.aifit.feature.gamification.domain.model.AchievementType
import com.jlsh.aifit.feature.gamification.domain.model.StreakStatus
import com.jlsh.aifit.feature.gamification.domain.model.StreakType
import com.jlsh.aifit.testutil.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class GamificationMapperTest {

    // ── StreakResponseDto.toDomain ──────────────────────────────────────────────

    @Test
    fun `toDomain convierte StreakResponseDto correctamente`() {
        val dto = fakeStreakResponseDto()
        val result = dto.toDomain()

        assertEquals(StreakType.TRAINING, result.type)
        assertEquals(StreakStatus.ACTIVE, result.status)
        assertEquals(5, result.currentCount)
        assertEquals(10, result.longestCount)
        assertEquals(LocalDate.of(2026, 4, 5), result.lastActivityDate)
        assertEquals("2026-03-01T10:00:00", result.startedAt)
    }

    @Test
    fun `toDomain mapea StreakType desconocido a UNKNOWN sin crash`() {
        val dto = fakeStreakResponseDto(type = "VALOR_INEXISTENTE")
        val result = dto.toDomain()
        assertEquals(StreakType.UNKNOWN, result.type)
    }

    @Test
    fun `toDomain mapea StreakStatus desconocido a UNKNOWN sin crash`() {
        val dto = fakeStreakResponseDto(status = "OTRO_STATUS")
        val result = dto.toDomain()
        assertEquals(StreakStatus.UNKNOWN, result.status)
    }

    @Test
    fun `toDomain usa fecha actual cuando lastActivityDate es invalida`() {
        val dto = fakeStreakResponseDto(lastActivityDate = "invalid-date")
        val result = dto.toDomain()
        assertEquals(LocalDate.now(), result.lastActivityDate)
    }

    // ── AchievementDefinitionResponseDto.toDomain ──────────────────────────────

    @Test
    fun `toDomain convierte AchievementDefinitionResponseDto correctamente`() {
        val dto = fakeAchievementDefinitionResponseDto()
        val result = dto.toDomain()

        assertEquals("ach-def-1", result.id)
        assertEquals("FIRST_WORKOUT", result.code)
        assertEquals(AchievementType.STRENGTH_MILESTONE, result.type)
        assertEquals("First Workout", result.name)
        assertEquals("Complete your first workout session.", result.description)
        assertEquals(AchievementRarity.COMMON, result.rarity)
        assertEquals("fitness_center", result.iconKey)
    }

    @Test
    fun `toDomain mapea AchievementType desconocido a UNKNOWN sin crash`() {
        val dto = fakeAchievementDefinitionResponseDto(type = "FUTURO_TIPO")
        val result = dto.toDomain()
        assertEquals(AchievementType.UNKNOWN, result.type)
    }

    @Test
    fun `toDomain mapea AchievementRarity desconocida a UNKNOWN sin crash`() {
        val dto = fakeAchievementDefinitionResponseDto(rarity = "MYTHIC")
        val result = dto.toDomain()
        assertEquals(AchievementRarity.UNKNOWN, result.rarity)
    }

    // ── UserAchievementResponseDto.toDomain ────────────────────────────────────

    @Test
    fun `toDomain convierte UserAchievementResponseDto correctamente`() {
        val dto = fakeUserAchievementResponseDto()
        val result = dto.toDomain()

        assertEquals("user-ach-1", result.id)
        assertEquals("ach-def-1", result.achievement.id)
        assertEquals("2026-04-01T10:00:00", result.unlockedAt)
        assertEquals("Completed first workout session", result.triggerDescription)
    }

    // ── PersonalRecordResponseDto.toDomain ─────────────────────────────────────

    @Test
    fun `toDomain convierte PersonalRecordResponseDto correctamente`() {
        val dto = fakePersonalRecordResponseDto()
        val result = dto.toDomain()

        assertEquals("pr-1", result.id)
        assertEquals("Bench Press", result.exerciseName)
        assertEquals(80.0, result.weightKg, 0.001)
        assertEquals(8, result.reps)
        assertEquals(100.0, result.estimatedOneRepMax, 0.001)
        assertEquals("2026-04-01T10:00:00", result.achievedAt)
    }

    // ── ProgressExportResponseDto.toDomain ─────────────────────────────────────

    @Test
    fun `toDomain convierte ProgressExportResponseDto correctamente`() {
        val dto = fakeProgressExportResponseDto()
        val result = dto.toDomain()

        assertEquals("user-1", result.userId)
        assertEquals("Test User", result.userName)
        assertEquals("LAST_MONTH", result.period)
        assertEquals(4, result.totalWorkouts) // sumOf trainingDaysCompleted
        assertEquals(1, result.totalPRs)
        assertEquals(5, result.currentStreak)
        assertEquals(1, result.achievementsUnlocked)
        assertEquals(-1.5, result.weightChange!!, 0.001)
        assertTrue(result.topExercises.first().contains("Bench Press"))
    }

    @Test
    fun `toDomain maneja ProgressExportResponseDto sin weightSummary`() {
        val dto = fakeProgressExportResponseDto(weightSummary = null)
        val result = dto.toDomain()
        assertNull(result.weightChange)
    }

    @Test
    fun `toDomain maneja ProgressExportResponseDto sin streaks retorna currentStreak 0`() {
        val dto = fakeProgressExportResponseDto(streaks = emptyList())
        val result = dto.toDomain()
        assertEquals(0, result.currentStreak)
    }
}

