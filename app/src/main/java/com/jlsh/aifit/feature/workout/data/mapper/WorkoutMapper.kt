package com.jlsh.aifit.feature.workout.data.mapper

import com.jlsh.aifit.feature.workout.data.dto.GamificationResultResponseDto
import com.jlsh.aifit.feature.workout.data.dto.JointPainEntryDto
import com.jlsh.aifit.feature.workout.data.dto.PersonalRecordResponseDto
import com.jlsh.aifit.feature.workout.data.dto.UserAchievementResponseDto
import com.jlsh.aifit.feature.workout.data.dto.WorkoutLogResponseDto
import com.jlsh.aifit.feature.workout.data.dto.WorkoutLogSummaryResponseDto
import com.jlsh.aifit.feature.workout.data.dto.WorkoutSetLogResponseDto
import com.jlsh.aifit.feature.workout.data.local.WorkoutLogEntity
import com.jlsh.aifit.feature.workout.domain.model.Achievement
import com.jlsh.aifit.feature.workout.domain.model.GamificationResult
import com.jlsh.aifit.feature.workout.domain.model.JointPainEntry
import com.jlsh.aifit.feature.workout.domain.model.JointZone
import com.jlsh.aifit.feature.workout.domain.model.PersonalRecord
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import com.jlsh.aifit.feature.workout.domain.model.WorkoutSetLog
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object WorkoutMapper {

    private val isoDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun WorkoutLogResponseDto.toDomain(): WorkoutLog = WorkoutLog(
        id = id,
        trainingPlanId = trainingPlanId,
        trainingDayId = trainingDayId,
        date = parseDate(date),
        durationMinutes = durationMinutes,
        perceivedExertion = perceivedExertion,
        notes = notes,
        totalExercises = exercises.map { it.trainingExerciseId }.distinct().size,
        completedAt = parseDateTime(completedAt),
        sets = exercises.map { it.toDomain() },
        gamificationResult = gamificationResult?.toDomain(),
        isLocked = isLocked,
        perceivedSystemicFatigue = perceivedSystemicFatigue,
        jointPainReport = jointPainReport?.mapNotNull { it.toDomain() } ?: emptyList(),
    )

    fun WorkoutLogSummaryResponseDto.toDomain(): WorkoutLog = WorkoutLog(
        id = id,
        trainingPlanId = trainingPlanId,
        trainingDayId = trainingDayId,
        date = parseDate(date),
        durationMinutes = durationMinutes,
        perceivedExertion = perceivedExertion,
        notes = null,
        totalExercises = totalExercises,
        completedAt = parseDateTime(completedAt),
        isLocked = isLocked,
    )

    fun WorkoutSetLogResponseDto.toDomain(): WorkoutSetLog = WorkoutSetLog(
        id = id,
        trainingExerciseId = trainingExerciseId,
        exerciseName = exerciseName,
        exerciseSetNumber = exerciseSetNumber,
        repsCompleted = repsCompleted,
        weightUsed = weightUsed,
        durationSeconds = durationSeconds,
        completed = completed,
        estimatedOneRepMax = estimatedOneRepMax,
        wasAutoregulated = wasAutoregulated,
        technicalNote = technicalNote,
        rpe = rpe,
    )

    fun GamificationResultResponseDto.toDomain(): GamificationResult = GamificationResult(
        newPersonalRecords = newPersonalRecords.map { it.toDomain() },
        unlockedAchievements = unlockedAchievements.map { it.toDomain() },
        updatedStreakCount = updatedStreak?.currentCount,
    )

    fun JointPainEntryDto.toDomain(): JointPainEntry? = try {
        JointPainEntry(
            zone = JointZone.valueOf(zone.uppercase()),
            note = note,
        )
    } catch (_: IllegalArgumentException) {
        null
    }

    fun PersonalRecordResponseDto.toDomain(): PersonalRecord = PersonalRecord(
        id = id,
        exerciseName = exerciseName,
        weightKg = weightKg,
        reps = reps,
        estimatedOneRepMax = estimatedOneRepMax,
        achievedAt = achievedAt,
    )

    fun UserAchievementResponseDto.toDomain(): Achievement = Achievement(
        id = id,
        code = achievement.code,
        type = achievement.type,
        name = achievement.name,
        description = achievement.description,
        rarity = achievement.rarity,
        iconKey = achievement.iconKey,
        unlockedAt = unlockedAt,
        triggerDescription = triggerDescription,
    )

    fun JointPainEntry.toDto(): JointPainEntryDto = JointPainEntryDto(
        zone = zone.name,
        note = note,
    )

    fun WorkoutLog.toEntity(): WorkoutLogEntity = WorkoutLogEntity(
            id = id,
            trainingPlanId = trainingPlanId,
            trainingDayId = trainingDayId,
            date = date.toEpochDay(),
            durationMinutes = durationMinutes,
            perceivedExertion = perceivedExertion,
            totalExercises = totalExercises,
            completedAt = completedAt.toInstant(ZoneOffset.UTC).toEpochMilli(),
            isLocked = isLocked,
        )

    fun WorkoutLogEntity.toDomain(): WorkoutLog = WorkoutLog(
            id = id,
            trainingPlanId = trainingPlanId,
            trainingDayId = trainingDayId,
            date = LocalDate.ofEpochDay(date),
            durationMinutes = durationMinutes,
            perceivedExertion = perceivedExertion,
            notes = null,
            totalExercises = totalExercises,
            completedAt = Instant.ofEpochMilli(completedAt).atZone(ZoneOffset.UTC).toLocalDateTime(),
            isLocked = isLocked,
        )

    private fun parseDateTime(raw: String): LocalDateTime =
        runCatching { LocalDateTime.parse(raw, isoDateTimeFormatter) }
            .getOrDefault(LocalDateTime.now())

    private fun parseDate(raw: String): LocalDate =
        runCatching { LocalDate.parse(raw, dateFormatter) }
            .getOrDefault(LocalDate.now())
}

