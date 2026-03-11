package com.jlsh.aifit.feature.nutrition.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionLogRepository
import javax.inject.Inject

class DeleteMealLogUseCase @Inject constructor(
    private val repository: NutritionLogRepository,
) {
    suspend operator fun invoke(mealId: String): Result<Unit> =
        repository.deleteMealLog(mealId)
}

