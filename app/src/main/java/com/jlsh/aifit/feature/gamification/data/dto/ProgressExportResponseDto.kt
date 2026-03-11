package com.jlsh.aifit.feature.gamification.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProgressExportResponseDto(
    val userId: String,
    val userName: String,
    val period: String,
    val generatedAt: String,
    val weightSummary: WeightSummaryExportDto? = null,
    val personalRecords: List<PersonalRecordResponseDto> = emptyList(),
    val weeklyAdherenceSummary: List<WeeklyAdherenceExportDto> = emptyList(),
    val streaks: List<StreakExportSummaryDto> = emptyList(),
    val unlockedAchievements: List<AchievementExportEntryDto> = emptyList(),
    val topExercisesProgression: List<ExerciseProgressionExportDto> = emptyList(),
)

@Serializable
data class WeightSummaryExportDto(
    val initialWeight: Double,
    val currentWeight: Double,
    val change: Double,
)

@Serializable
data class WeeklyAdherenceExportDto(
    val weekStart: String,
    val trainingDaysCompleted: Int,
    val trainingDaysPlanned: Int,
    val nutritionDaysTracked: Int,
)

@Serializable
data class StreakExportSummaryDto(
    val type: String,
    val currentCount: Int,
    val longestCount: Int,
    val status: String,
)

@Serializable
data class AchievementExportEntryDto(
    val name: String,
    val rarity: String,
    val unlockedAt: String,
)

@Serializable
data class ExerciseProgressionExportDto(
    val exerciseName: String,
    val initialBestWeightKg: Double,
    val currentBestWeightKg: Double,
    val progressionPercentage: Double,
)

