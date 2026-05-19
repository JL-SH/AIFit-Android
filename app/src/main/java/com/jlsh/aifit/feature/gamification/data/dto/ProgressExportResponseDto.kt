package com.jlsh.aifit.feature.gamification.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProgressExportResponseDto(
    val userId: String = "",
    val userName: String = "",
    val period: String = "",
    val generatedAt: String = "",
    val weightSummary: WeightSummaryExportDto? = null,
    val personalRecords: List<PersonalRecordResponseDto> = emptyList(),
    val weeklyAdherenceSummary: List<WeeklyAdherenceExportDto> = emptyList(),
    val streaks: List<StreakExportSummaryDto> = emptyList(),
    val unlockedAchievements: List<AchievementExportEntryDto> = emptyList(),
    val topExercisesProgression: List<ExerciseProgressionExportDto> = emptyList(),
)

@Serializable
data class WeightSummaryExportDto(
    val initialWeight: Double = 0.0,
    val currentWeight: Double = 0.0,
    val change: Double = 0.0,
)

@Serializable
data class WeeklyAdherenceExportDto(
    val weekStart: String = "",
    val trainingDaysCompleted: Int = 0,
    val trainingDaysPlanned: Int = 0,
    val nutritionDaysTracked: Int = 0,
)

@Serializable
data class StreakExportSummaryDto(
    val type: String = "UNKNOWN",
    val currentCount: Int = 0,
    val longestCount: Int = 0,
    val status: String = "UNKNOWN",
)

@Serializable
data class AchievementExportEntryDto(
    val name: String = "",
    val rarity: String = "UNKNOWN",
    val unlockedAt: String = "",
)

@Serializable
data class ExerciseProgressionExportDto(
    val exerciseName: String = "",
    val initialBestWeightKg: Double = 0.0,
    val currentBestWeightKg: Double = 0.0,
    val progressionPercentage: Double = 0.0,
)
