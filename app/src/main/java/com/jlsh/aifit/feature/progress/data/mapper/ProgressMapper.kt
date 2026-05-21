package com.jlsh.aifit.feature.progress.data.mapper

import com.jlsh.aifit.feature.progress.data.dto.BodyWeightLogResponseDto
import com.jlsh.aifit.feature.progress.data.dto.NutritionAdherenceResponseDto
import com.jlsh.aifit.feature.progress.data.dto.ProgressDashboardResponseDto
import com.jlsh.aifit.feature.progress.data.dto.StrengthProgressResponseDto
import com.jlsh.aifit.feature.progress.data.dto.WeeklyProgressSummaryResponseDto
import com.jlsh.aifit.feature.progress.data.dto.WeightProgressResponseDto
import com.jlsh.aifit.feature.progress.data.dto.WorkoutAdherenceResponseDto
import com.jlsh.aifit.feature.progress.data.local.BodyWeightEntity
import com.jlsh.aifit.feature.progress.domain.model.BodyWeightLog
import com.jlsh.aifit.feature.progress.domain.model.NutritionAdherence
import com.jlsh.aifit.feature.progress.domain.model.ProgressDashboard
import com.jlsh.aifit.feature.progress.domain.model.StrengthProgress
import com.jlsh.aifit.feature.progress.domain.model.WeeklyProgressSummary
import com.jlsh.aifit.feature.progress.domain.model.WeightEntry
import com.jlsh.aifit.feature.progress.domain.model.WeightProgress
import com.jlsh.aifit.feature.progress.domain.model.WeightTrend
import com.jlsh.aifit.feature.progress.domain.model.WorkoutAdherence
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ProgressMapper {

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun ProgressDashboardResponseDto.toDomain(): ProgressDashboard = ProgressDashboard(
        periodFrom = parseDate(period.from),
        periodTo = parseDate(period.to),
        workoutAdherence = workoutAdherence.toDomain(),
        weightProgress = weightProgress.toDomain(),
        nutritionAdherence = nutritionAdherence.toDomain(),
        strengthProgress = strengthProgress.mapNotNull { it.toDomain() },
    )

    fun WorkoutAdherenceResponseDto.toDomain(): WorkoutAdherence = WorkoutAdherence(
        plannedSessions = plannedSessions,
        completedSessions = completedSessions,
        adherencePercentage = adherencePercentage,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
    )

    fun WeightProgressResponseDto.toDomain(): WeightProgress = WeightProgress(
        startWeight = initialWeight,
        currentWeight = currentWeight,
        targetWeight = targetWeight,
        change = change,
        trend = WeightTrend.fromString(trend),
        entries = entries.map { WeightEntry(date = parseDate(it.date), weight = it.weight) },
    )

    fun NutritionAdherenceResponseDto.toDomain(): NutritionAdherence = NutritionAdherence(
        averageCalories = averageCaloriesConsumed,
        calorieTarget = targetCalories,
        adherencePercentage = calorieAdherencePercentage,
    )

    fun StrengthProgressResponseDto.toDomain(): StrengthProgress? {
        val start = bestSetStart?.weight ?: return null
        val end = bestSetEnd?.weight ?: return null
        return StrengthProgress(
            exerciseName = exerciseName,
            startMax = start,
            currentMax = end,
            changePercentage = progressionPercentage ?: 0.0,
        )
    }

    fun WeeklyProgressSummaryResponseDto.toDomain(): WeeklyProgressSummary = WeeklyProgressSummary(
        workoutsThisWeek = workoutsThisWeek,
        workoutsTarget = workoutsTarget,
        averageCaloriesToday = averageCaloriesToday,
        calorieTarget = calorieTarget,
        currentStreak = currentStreak,
        bodyWeight = bodyWeight,
    )

    fun BodyWeightLogResponseDto.toDomain(): BodyWeightLog = BodyWeightLog(
        id = id,
        weight = weight,
        date = parseDate(date),
        notes = notes,
        createdAt = parseDate(createdAt.take(10)),
    )

    fun BodyWeightLog.toEntity(): BodyWeightEntity = BodyWeightEntity(
        id = id,
        weight = weight,
        date = date.toEpochDay(),
        notes = notes,
        createdAt = createdAt.toEpochDay(),
    )

    fun BodyWeightEntity.toDomain(): BodyWeightLog = BodyWeightLog(
        id = id,
        weight = weight,
        date = LocalDate.ofEpochDay(date),
        notes = notes,
        createdAt = LocalDate.ofEpochDay(createdAt),
    )

    private fun parseDate(raw: String): LocalDate =
        runCatching { LocalDate.parse(raw, dateFormatter) }
            .getOrDefault(LocalDate.now())
}


