package com.jlsh.aifit.feature.user.data.dto

import com.jlsh.aifit.feature.diet.data.dto.DietPlanResponseDto
import com.jlsh.aifit.feature.nutrition.data.dto.NutritionTargetResponseDto
import com.jlsh.aifit.feature.training.data.dto.TrainingPlanResponseDto
import kotlinx.serialization.Serializable

@Serializable
data class OnboardingResultDto(
    val trainingPlan: TrainingPlanResponseDto,
    val dietPlan: DietPlanResponseDto,
    val nutritionTarget: NutritionTargetResponseDto,
)

