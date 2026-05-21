package com.jlsh.aifit.feature.progress.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class PeriodResponseDto(
    val from: String,
    val to: String,
)

@Serializable
data class WeeklyAdherenceDto(
    val weekStart: String,
    val planned: Int,
    val completed: Int,
    val percentage: Double,
)

@Serializable
data class WeightEntryDto(
    val date: String,
    val weight: Double,
)

@Serializable
data class BestSetResponseDto(
    val date: String,
    val reps: Int,
    val weight: Double? = null,
)

@Serializable
data class ProgressDashboardResponseDto(
    val period: PeriodResponseDto,
    val workoutAdherence: WorkoutAdherenceResponseDto,
    val weightProgress: WeightProgressResponseDto,
    val nutritionAdherence: NutritionAdherenceResponseDto,
    val strengthProgress: List<StrengthProgressResponseDto>,
    val generatedAt: String,
)
