package com.jlsh.aifit.feature.progress.domain.model

import java.time.LocalDate

data class ProgressDashboard(
    val periodFrom: LocalDate,
    val periodTo: LocalDate,
    val workoutAdherence: WorkoutAdherence,
    val weightProgress: WeightProgress,
    val nutritionAdherence: NutritionAdherence,
    val strengthProgress: List<StrengthProgress>,
)

