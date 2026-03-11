package com.jlsh.aifit.feature.home.ui.state

import com.jlsh.aifit.feature.gamification.domain.model.Streak
import com.jlsh.aifit.feature.progress.domain.model.BodyWeightLog
import com.jlsh.aifit.feature.progress.domain.model.WeeklyProgressSummary

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Error(val message: String) : HomeUiState()
    data class Success(
        val userName: String,
        val avatarUrl: String?,
        val todayTraining: TodayTrainingState?,
        val todayNutrition: TodayNutritionState?,
        val streaks: List<Streak>,
        val weeklySummary: WeeklyProgressSummary?,
        val weightEntries: List<BodyWeightLog>,
    ) : HomeUiState()
}

data class TodayTrainingState(
    val planId: String,
    val planName: String,
    val dayName: String,
    val exerciseCount: Int,
    val adherencePercentage: Float,
)

data class TodayNutritionState(
    val caloriesConsumed: Int,
    val calorieTarget: Int,
    val proteinConsumed: Double,
    val proteinTarget: Double,
    val carbsConsumed: Double,
    val carbsTarget: Double,
    val fatConsumed: Double,
    val fatTarget: Double,
)

