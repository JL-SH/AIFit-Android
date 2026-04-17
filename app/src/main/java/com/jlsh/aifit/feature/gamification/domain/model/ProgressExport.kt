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
    LAST_THREE_MONTHS("LAST_THREE_MONTHS"),
    ALL_TIME("ALL_TIME");
}

fun ExportPeriod.toDisplayString(): String = when (this) {
    ExportPeriod.LAST_WEEK -> "Última semana"
    ExportPeriod.LAST_MONTH -> "Último mes"
    ExportPeriod.LAST_THREE_MONTHS -> "Últimos 3 meses"
    ExportPeriod.ALL_TIME -> "Todo el historial"
}

fun String.toExportPeriodDisplayString(): String = when (this) {
    "LAST_WEEK" -> "Última semana"
    "LAST_MONTH" -> "Último mes"
    "LAST_THREE_MONTHS" -> "Últimos 3 meses"
    "ALL_TIME" -> "Todo el historial"
    else -> this
}

