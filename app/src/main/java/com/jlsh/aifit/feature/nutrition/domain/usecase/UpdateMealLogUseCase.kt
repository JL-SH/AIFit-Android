package com.jlsh.aifit.feature.nutrition.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.nutrition.data.dto.UpdateMealRequestDto
import com.jlsh.aifit.feature.nutrition.domain.model.MealLog
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionLogRepository
import javax.inject.Inject

class UpdateMealLogUseCase @Inject constructor(
    private val repository: NutritionLogRepository,
) {
    suspend operator fun invoke(mealId: String, request: UpdateMealRequestDto): Result<MealLog> =
        repository.updateMealLog(mealId, request)
}
