package com.jlsh.aifit.feature.home.domain.model

import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.gamification.domain.model.AchievementDefinition
import com.jlsh.aifit.feature.gamification.domain.model.Streak
import com.jlsh.aifit.feature.gamification.domain.model.UserAchievement
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionLog
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget
import com.jlsh.aifit.feature.progress.domain.model.WeeklyProgressSummary
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog

data class HomeBootstrap(
    val profile: UserProfile,
    val activeTrainingPlan: TrainingPlan?,
    val activeDietPlan: DietPlan? = null,
    val nutritionLog: NutritionLog?,
    val nutritionTarget: NutritionTarget?,
    val weeklySummary: WeeklyProgressSummary,
    val streaks: List<Streak>,
    val achievements: List<UserAchievement>,
    val todayWorkouts: List<WorkoutLog>,
    val achievementDefinitions: List<AchievementDefinition> = emptyList(),
)
