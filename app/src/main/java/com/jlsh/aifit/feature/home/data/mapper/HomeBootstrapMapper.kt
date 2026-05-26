package com.jlsh.aifit.feature.home.data.mapper

import com.jlsh.aifit.feature.diet.data.mapper.DietMapper.toDomain
import com.jlsh.aifit.feature.gamification.data.mapper.GamificationMapper.toDomain
import com.jlsh.aifit.feature.home.data.dto.HomeBootstrapResponseDto
import com.jlsh.aifit.feature.home.domain.model.HomeBootstrap
import com.jlsh.aifit.feature.nutrition.data.mapper.NutritionMapper.toDomain
import com.jlsh.aifit.feature.progress.data.mapper.ProgressMapper.toDomain
import com.jlsh.aifit.feature.training.data.mapper.TrainingMapper.toDomain
import com.jlsh.aifit.feature.user.data.mapper.UserMapper.toDomain
import com.jlsh.aifit.feature.workout.data.mapper.WorkoutMapper.toDomain

object HomeBootstrapMapper {

    fun HomeBootstrapResponseDto.toDomain(): HomeBootstrap = HomeBootstrap(
        profile = profile.toDomain(),
        activeTrainingPlan = activeTrainingPlan?.toDomain(),
        activeDietPlan = activeDietPlan?.toDomain(),
        nutritionLog = nutritionToday?.toDomain(),
        nutritionTarget = nutritionTarget?.toDomain(),
        weeklySummary = weeklyProgress.toDomain(),
        streaks = streaks.map { it.toDomain() },
        achievements = achievements.map { it.toDomain() },
        todayWorkouts = todayWorkouts.map { it.toDomain() },
        achievementDefinitions = achievementDefinitions.map { it.toDomain() },
    )
}
