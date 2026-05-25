package com.jlsh.aifit.feature.training.data.mapper

import com.jlsh.aifit.feature.training.data.dto.ExerciseSubstitutionResponseDto
import com.jlsh.aifit.feature.training.data.dto.TrainingDayResponseDto
import com.jlsh.aifit.feature.training.data.dto.TrainingExerciseResponseDto
import com.jlsh.aifit.feature.training.data.dto.TrainingPlanResponseDto
import com.jlsh.aifit.feature.training.data.dto.TrainingPlanSummaryResponseDto
import com.jlsh.aifit.feature.training.data.dto.WarmUpExerciseResponseDto
import com.jlsh.aifit.feature.training.data.dto.WarmUpProtocolResponseDto
import com.jlsh.aifit.feature.training.data.local.TrainingPlanEntity
import com.jlsh.aifit.feature.training.domain.model.ExerciseSubstitution
import com.jlsh.aifit.feature.training.domain.model.MuscleGroup
import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.training.domain.model.TrainingDay
import com.jlsh.aifit.feature.training.domain.model.TrainingDayType
import com.jlsh.aifit.feature.training.domain.model.TrainingExercise
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import com.jlsh.aifit.feature.training.domain.model.WarmUpExercise
import com.jlsh.aifit.feature.training.domain.model.WarmUpProtocol
import com.jlsh.aifit.feature.user.domain.model.FitnessLevel
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.WorkoutLocation
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Mappings between network DTOs, Room entities and domain models of the training module.
 */
object TrainingMapper {

    private val isoFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    /**
     * Converts a plan summary from the API to domain (without detailed days).
     *
     * @return [TrainingPlan] with [TrainingPlan.days] empty.
     */
    fun TrainingPlanSummaryResponseDto.toDomain(): TrainingPlan = TrainingPlan(
        id = id,
        name = name,
        description = description,
        frequencyDaysPerWeek = frequencyDaysPerWeek,
        durationWeeks = durationWeeks,
        goalType = GoalType.fromString(goalType),
        fitnessLevel = FitnessLevel.fromString(fitnessLevel),
        location = WorkoutLocation.fromString(location),
        status = PlanStatus.fromString(status),
        totalDays = totalDays,
        createdAt = parseDateTime(createdAt),
        days = emptyList(),
    )

    /**
     * Convert the full plan answer, including days and exercises.
     */
    fun TrainingPlanResponseDto.toDomain(): TrainingPlan = TrainingPlan(
        id = id,
        name = name,
        description = description,
        frequencyDaysPerWeek = frequencyDaysPerWeek,
        durationWeeks = durationWeeks,
        goalType = GoalType.fromString(goalType),
        fitnessLevel = FitnessLevel.fromString(fitnessLevel),
        location = WorkoutLocation.fromString(location),
        status = PlanStatus.fromString(status),
        totalDays = totalDays ?: days.size,
        createdAt = parseDateTime(createdAt),
        days = days.map { it.toDomain() },
    )

    /** Maps a training day from the DTO to the domain model.*/
    fun TrainingDayResponseDto.toDomain(): TrainingDay = TrainingDay(
        id = id,
        dayNumber = dayNumber,
        name = name,
        estimatedDurationMinutes = estimatedDurationMinutes,
        exercises = exercises.map { it.toDomain() },
        dayOfWeek = dayOfWeek?.let {
            try {
                DayOfWeek.valueOf(it.uppercase())
            } catch (_: IllegalArgumentException) {
                null
            }
        },
        dayType = dayType?.let {
            try {
                TrainingDayType.valueOf(it.uppercase())
            } catch (_: IllegalArgumentException) {
                TrainingDayType.TRAINING
            }
        } ?: TrainingDayType.TRAINING,
    )

    /** Maps a prescribed exercise from the DTO to the domain model.*/
    fun TrainingExerciseResponseDto.toDomain(): TrainingExercise = TrainingExercise(
        id = id,
        name = name,
        description = description,
        primaryMuscle = MuscleGroup.fromString(primaryMuscle),
        secondaryMuscle = secondaryMuscle?.let { MuscleGroup.fromString(it) },
        sets = sets,
        repsMin = repsMin,
        repsMax = repsMax,
        restSeconds = restSeconds,
        notes = notes,
        order = order,
        targetRpe = targetRpe,
        requiresExternalWeight = requiresExternalWeight,
    )

    /**
     * Persists a domain plan as a Room entity for the given user.
     *
     * @param userId Identifier of the user who owns the plan.
     */
    fun TrainingPlan.toEntity(userId: String): TrainingPlanEntity = TrainingPlanEntity(
        id = id,
        userId = userId,
        name = name,
        description = description,
        status = status.name,
        frequencyDaysPerWeek = frequencyDaysPerWeek,
        durationWeeks = durationWeeks,
        goalType = goalType.name,
        fitnessLevel = fitnessLevel.name,
        location = location.name,
        totalDays = totalDays,
        createdAt = createdAt.toInstant(ZoneOffset.UTC).toEpochMilli(),
    )

    /**
     * Restores a plan from local cache (without detailed days).
     */
    fun TrainingPlanEntity.toDomain(): TrainingPlan = TrainingPlan(
        id = id,
        name = name,
        description = description,
        frequencyDaysPerWeek = frequencyDaysPerWeek,
        durationWeeks = durationWeeks,
        goalType = GoalType.fromString(goalType),
        fitnessLevel = FitnessLevel.fromString(fitnessLevel),
        location = WorkoutLocation.fromString(location),
        status = PlanStatus.fromString(status),
        totalDays = totalDays,
        createdAt = Instant.ofEpochMilli(createdAt).atZone(ZoneOffset.UTC).toLocalDateTime(),
        days = emptyList(),
    )

    /** Maps the warmup protocol from the API to the domain.*/
    fun WarmUpProtocolResponseDto.toDomain(): WarmUpProtocol = WarmUpProtocol(
        trainingDayId = trainingDayId,
        estimatedTotalLoad = estimatedTotalLoad,
        exercises = exercises.map { it.toDomain() },
    )

    /** Map an individual warm-up exercise.*/
    fun WarmUpExerciseResponseDto.toDomain(): WarmUpExercise = WarmUpExercise(
        name = name,
        description = description,
        sets = sets,
        reps = reps,
        durationSeconds = durationSeconds,
    )

    /** Maps an exercise alternative suggested by the backend.*/
    fun ExerciseSubstitutionResponseDto.toDomain(): ExerciseSubstitution = ExerciseSubstitution(
        name = name,
        primaryMuscle = MuscleGroup.fromString(primaryMuscle),
        movementPattern = movementPattern,
        description = description,
    )

    private fun parseDateTime(raw: String): LocalDateTime =
        runCatching { LocalDateTime.parse(raw, isoFormatter) }
            .getOrDefault(LocalDateTime.now())
}

