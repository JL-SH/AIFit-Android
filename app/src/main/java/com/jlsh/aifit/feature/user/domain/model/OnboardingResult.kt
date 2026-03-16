package com.jlsh.aifit.feature.user.domain.model

import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan

data class OnboardingResult(
    val trainingPlan: TrainingPlan,
    val dietPlan: DietPlan,
    val nutritionTarget: NutritionTarget,
)

