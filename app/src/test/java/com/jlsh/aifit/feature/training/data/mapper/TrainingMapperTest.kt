package com.jlsh.aifit.feature.training.data.mapper

import com.jlsh.aifit.feature.training.data.mapper.TrainingMapper.toDomain
import com.jlsh.aifit.feature.training.data.mapper.TrainingMapper.toEntity
import com.jlsh.aifit.feature.training.domain.model.MuscleGroup
import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.training.domain.model.TrainingDayType
import com.jlsh.aifit.feature.user.domain.model.FitnessLevel
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.WorkoutLocation
import com.jlsh.aifit.testutil.*
import org.junit.Assert.*
import org.junit.Test

class TrainingMapperTest {

    // ─── SummaryDto → Domain ───────────────────────────────────────────────────

    @Test
    fun `toDomain convierte summary DTO con todos los campos`() {
        val dto = fakeTrainingPlanSummaryResponseDto()

        val result = dto.toDomain()

        assertEquals("plan-1", result.id)
        assertEquals("Test Plan", result.name)
        assertEquals(4, result.frequencyDaysPerWeek)
        assertEquals(8, result.durationWeeks)
        assertEquals(GoalType.GAIN_MUSCLE, result.goalType)
        assertEquals(FitnessLevel.INTERMEDIATE, result.fitnessLevel)
        assertEquals(WorkoutLocation.GYM, result.location)
        assertEquals(PlanStatus.ACTIVE, result.status)
        assertEquals(32, result.totalDays)
        assertTrue(result.days.isEmpty())
    }

    // ─── PlanResponseDto → Domain ──────────────────────────────────────────────

    @Test
    fun `toDomain convierte plan DTO con days y exercises`() {
        val dto = fakeTrainingPlanResponseDto()

        val result = dto.toDomain()

        assertEquals("plan-1", result.id)
        assertEquals(1, result.days.size)
        assertEquals("Push Day", result.days[0].name)
        assertEquals(1, result.days[0].exercises.size)
        assertEquals("Bench Press", result.days[0].exercises[0].name)
    }

    // ─── DayResponseDto → Domain ───────────────────────────────────────────────

    @Test
    fun `toDomain convierte day DTO con ejercicios`() {
        val dto = fakeTrainingDayResponseDto()

        val result = dto.toDomain()

        assertEquals("day-1", result.id)
        assertEquals(1, result.dayNumber)
        assertEquals("Push Day", result.name)
        assertEquals(60, result.estimatedDurationMinutes)
        assertEquals(TrainingDayType.TRAINING, result.dayType)
        assertEquals(1, result.exercises.size)
    }

    @Test
    fun `toDomain mapea dayOfWeek válido correctamente`() {
        val dto = fakeTrainingDayResponseDto().copy(dayOfWeek = "MONDAY")

        val result = dto.toDomain()

        assertEquals(java.time.DayOfWeek.MONDAY, result.dayOfWeek)
    }

    @Test
    fun `toDomain mapea dayOfWeek inválido a null sin crash`() {
        val dto = fakeTrainingDayResponseDto().copy(dayOfWeek = "NOTADAY")

        val result = dto.toDomain()

        assertNull(result.dayOfWeek)
    }

    @Test
    fun `toDomain mapea dayType desconocido a TRAINING por defecto`() {
        val dto = fakeTrainingDayResponseDto().copy(dayType = "YOGA_FUSION")

        val result = dto.toDomain()

        assertEquals(TrainingDayType.TRAINING, result.dayType)
    }

    @Test
    fun `toDomain mapea dayType REST correctamente`() {
        val dto = fakeTrainingDayResponseDto().copy(dayType = "REST")

        val result = dto.toDomain()

        assertEquals(TrainingDayType.REST, result.dayType)
    }

    // ─── ExerciseResponseDto → Domain ──────────────────────────────────────────

    @Test
    fun `toDomain convierte exercise DTO con todos los campos`() {
        val dto = fakeTrainingExerciseResponseDto()

        val result = dto.toDomain()

        assertEquals("exercise-1", result.id)
        assertEquals("Bench Press", result.name)
        assertEquals(MuscleGroup.CHEST, result.primaryMuscle)
        assertEquals(MuscleGroup.TRICEPS, result.secondaryMuscle)
        assertEquals(4, result.sets)
        assertEquals(8, result.repsMin)
        assertEquals(12, result.repsMax)
        assertEquals(90, result.restSeconds)
        assertEquals(1, result.order)
    }

    @Test
    fun `toDomain mapea secondaryMuscle null correctamente`() {
        val dto = fakeTrainingExerciseResponseDto(secondaryMuscle = null)

        val result = dto.toDomain()

        assertNull(result.secondaryMuscle)
    }

    // ─── Enum fallback UNKNOWN ─────────────────────────────────────────────────

    @Test
    fun `toDomain mapea PlanStatus desconocido a UNKNOWN sin crash`() {
        val dto = fakeTrainingPlanSummaryResponseDto().copy(status = "VALOR_INEXISTENTE")

        val result = dto.toDomain()

        assertEquals(PlanStatus.UNKNOWN, result.status)
    }

    @Test
    fun `toDomain mapea MuscleGroup desconocido a UNKNOWN sin crash`() {
        val dto = fakeTrainingExerciseResponseDto(primaryMuscle = "ALIEN_MUSCLE")

        val result = dto.toDomain()

        assertEquals(MuscleGroup.UNKNOWN, result.primaryMuscle)
    }

    @Test
    fun `toDomain mapea GoalType desconocido a UNKNOWN sin crash`() {
        val dto = fakeTrainingPlanSummaryResponseDto().copy(goalType = "FLY_TO_MOON")

        val result = dto.toDomain()

        assertEquals(GoalType.UNKNOWN, result.goalType)
    }

    @Test
    fun `toDomain mapea FitnessLevel desconocido a UNKNOWN sin crash`() {
        val dto = fakeTrainingPlanSummaryResponseDto().copy(fitnessLevel = "SUPERHERO")

        val result = dto.toDomain()

        assertEquals(FitnessLevel.UNKNOWN, result.fitnessLevel)
    }

    @Test
    fun `toDomain mapea WorkoutLocation desconocido a UNKNOWN sin crash`() {
        val dto = fakeTrainingPlanSummaryResponseDto().copy(location = "MARS")

        val result = dto.toDomain()

        assertEquals(WorkoutLocation.UNKNOWN, result.location)
    }

    // ─── Domain → Entity ───────────────────────────────────────────────────────

    @Test
    fun `toEntity convierte plan de dominio a entity`() {
        val plan = fakeTrainingPlan()

        val result = plan.toEntity("user-1")

        assertEquals("plan-1", result.id)
        assertEquals("user-1", result.userId)
        assertEquals("Test Plan", result.name)
        assertEquals("ACTIVE", result.status)
        assertEquals(4, result.frequencyDaysPerWeek)
        assertEquals("GAIN_MUSCLE", result.goalType)
        assertEquals("INTERMEDIATE", result.fitnessLevel)
        assertEquals("GYM", result.location)
    }

    // ─── Entity → Domain ───────────────────────────────────────────────────────

    @Test
    fun `toDomain convierte entity a plan con days vacías`() {
        val entity = fakeTrainingPlanEntity()

        val result = entity.toDomain()

        assertEquals("plan-1", result.id)
        assertEquals("Test Plan", result.name)
        assertEquals(PlanStatus.ACTIVE, result.status)
        assertTrue(result.days.isEmpty())
    }

    @Test
    fun `toDomain de entity con status desconocido mapea a UNKNOWN`() {
        val entity = fakeTrainingPlanEntity(status = "NONSENSE")

        val result = entity.toDomain()

        assertEquals(PlanStatus.UNKNOWN, result.status)
    }

    // ─── Round-trip ────────────────────────────────────────────────────────────

    @Test
    fun `round-trip domain a entity y back preserva campos clave`() {
        val original = fakeTrainingPlan()

        val entity = original.toEntity("user-1")
        val restored = entity.toDomain()

        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.status, restored.status)
        assertEquals(original.goalType, restored.goalType)
        assertEquals(original.fitnessLevel, restored.fitnessLevel)
    }
}

