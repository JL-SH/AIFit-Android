package com.jlsh.aifit.feature.progress.domain.model

data class WeeklyProgressSummary(
    val workoutsThisWeek: Int,
    val workoutsTarget: Int,
    val averageCaloriesToday: Double,
    val calorieTarget: Int,
    val currentStreak: Int,
    // TODO: eliminar si no se usa en otro sitio — el peso ya se muestra en BodyWeightScreen (UC-09)
    val bodyWeight: Double?,
)

