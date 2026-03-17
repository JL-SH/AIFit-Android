package com.jlsh.aifit.feature.training.data.mapper

import com.jlsh.aifit.feature.training.data.dto.TrainingDayResponseDto
import com.jlsh.aifit.feature.training.data.dto.TrainingExerciseResponseDto
import com.jlsh.aifit.feature.training.data.dto.TrainingPlanResponseDto
import com.jlsh.aifit.feature.training.data.dto.TrainingPlanSummaryResponseDto
import com.jlsh.aifit.feature.training.data.local.TrainingPlanEntity
import com.jlsh.aifit.feature.training.domain.model.MuscleGroup
import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.training.domain.model.TrainingDay
import com.jlsh.aifit.feature.training.domain.model.TrainingExercise
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import com.jlsh.aifit.feature.user.domain.model.FitnessLevel
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.WorkoutLocation
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object TrainingMapper {

    private val isoFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

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
    )

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
    )

    fun TrainingPlan.toEntity(): TrainingPlanEntity = TrainingPlanEntity(
        id = id,
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

    private fun parseDateTime(raw: String): LocalDateTime =
        runCatching { LocalDateTime.parse(raw, isoFormatter) }
            .getOrDefault(LocalDateTime.now())
}

