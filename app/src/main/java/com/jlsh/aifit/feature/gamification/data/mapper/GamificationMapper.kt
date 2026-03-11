package com.jlsh.aifit.feature.gamification.data.mapper

import com.jlsh.aifit.feature.gamification.data.dto.AchievementDefinitionResponseDto
import com.jlsh.aifit.feature.gamification.data.dto.PersonalRecordResponseDto
import com.jlsh.aifit.feature.gamification.data.dto.ProgressExportResponseDto
import com.jlsh.aifit.feature.gamification.data.dto.StreakResponseDto
import com.jlsh.aifit.feature.gamification.data.dto.UserAchievementResponseDto
import com.jlsh.aifit.feature.gamification.domain.model.AchievementDefinition
import com.jlsh.aifit.feature.gamification.domain.model.AchievementRarity
import com.jlsh.aifit.feature.gamification.domain.model.AchievementType
import com.jlsh.aifit.feature.gamification.domain.model.PersonalRecord
import com.jlsh.aifit.feature.gamification.domain.model.ProgressExport
import com.jlsh.aifit.feature.gamification.domain.model.Streak
import com.jlsh.aifit.feature.gamification.domain.model.StreakStatus
import com.jlsh.aifit.feature.gamification.domain.model.StreakType
import com.jlsh.aifit.feature.gamification.domain.model.UserAchievement
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object GamificationMapper {

    private val dateFormatter = DateTimeFormatter.ISO_DATE

    fun StreakResponseDto.toDomain(): Streak = Streak(
        type = StreakType.fromString(type),
        status = StreakStatus.fromString(status),
        currentCount = currentCount,
        longestCount = longestCount,
        lastActivityDate = runCatching { LocalDate.parse(lastActivityDate, dateFormatter) }
            .getOrDefault(LocalDate.now()),
        startedAt = startedAt,
    )

    fun AchievementDefinitionResponseDto.toDomain(): AchievementDefinition = AchievementDefinition(
        id = id,
        code = code,
        type = AchievementType.fromString(type),
        name = name,
        description = description,
        rarity = AchievementRarity.fromString(rarity),
        iconKey = iconKey,
    )

    fun UserAchievementResponseDto.toDomain(): UserAchievement = UserAchievement(
        id = id,
        achievement = achievement.toDomain(),
        unlockedAt = unlockedAt,
        triggerDescription = triggerDescription,
    )

    fun PersonalRecordResponseDto.toDomain(): PersonalRecord = PersonalRecord(
        id = id,
        exerciseName = exerciseName,
        weightKg = weightKg,
        reps = reps,
        estimatedOneRepMax = estimatedOneRepMax,
        achievedAt = achievedAt,
    )

    fun ProgressExportResponseDto.toDomain(): ProgressExport = ProgressExport(
        userId = userId,
        userName = userName,
        period = period,
        generatedAt = generatedAt,
        totalWorkouts = weeklyAdherenceSummary.sumOf { it.trainingDaysCompleted },
        totalPRs = personalRecords.size,
        currentStreak = streaks.maxOfOrNull { it.currentCount } ?: 0,
        achievementsUnlocked = unlockedAchievements.size,
        weightChange = weightSummary?.change,
        topExercises = topExercisesProgression.map { "${it.exerciseName}: ${it.progressionPercentage.toInt()}%" },
    )
}

