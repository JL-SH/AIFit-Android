package com.jlsh.aifit.feature.home.data.dto

import com.jlsh.aifit.feature.diet.data.dto.DietPlanResponseDto
import com.jlsh.aifit.feature.gamification.data.dto.AchievementDefinitionResponseDto
import com.jlsh.aifit.feature.gamification.data.dto.StreakResponseDto
import com.jlsh.aifit.feature.gamification.data.dto.UserAchievementResponseDto
import com.jlsh.aifit.feature.nutrition.data.dto.NutritionLogResponseDto
import com.jlsh.aifit.feature.nutrition.data.dto.NutritionTargetResponseDto
import com.jlsh.aifit.feature.progress.data.dto.WeeklyProgressSummaryResponseDto
import com.jlsh.aifit.feature.training.data.dto.TrainingPlanResponseDto
import com.jlsh.aifit.feature.user.data.dto.UserProfileResponseDto
import com.jlsh.aifit.feature.workout.data.dto.WorkoutLogSummaryResponseDto
import kotlinx.serialization.Serializable

@Serializable
data class HomeBootstrapResponseDto(
    val profile: UserProfileResponseDto,
    val activeTrainingPlan: TrainingPlanResponseDto? = null,
    val activeDietPlan: DietPlanResponseDto? = null,
    val nutritionToday: NutritionLogResponseDto? = null,
    val nutritionTarget: NutritionTargetResponseDto? = null,
    val weeklyProgress: WeeklyProgressSummaryResponseDto,
    val streaks: List<StreakResponseDto> = emptyList(),
    val achievements: List<UserAchievementResponseDto> = emptyList(),
    val todayWorkouts: List<WorkoutLogSummaryResponseDto> = emptyList(),
    val achievementDefinitions: List<AchievementDefinitionResponseDto> = emptyList(),
)
