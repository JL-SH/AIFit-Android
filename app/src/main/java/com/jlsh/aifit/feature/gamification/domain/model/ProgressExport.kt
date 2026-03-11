package com.jlsh.aifit.feature.gamification.domain.model

data class ProgressExport(
    val userId: String,
    val userName: String,
    val period: String,
    val generatedAt: String,
    val totalWorkouts: Int,
    val totalPRs: Int,
    val currentStreak: Int,
    val achievementsUnlocked: Int,
    val weightChange: Double?,
    val topExercises: List<String>,
)

enum class ExportPeriod(val apiValue: String) {
    LAST_WEEK("LAST_WEEK"),
    LAST_MONTH("LAST_MONTH"),
    LAST_3_MONTHS("LAST_3_MONTHS"),
    ALL_TIME("ALL_TIME");
}

