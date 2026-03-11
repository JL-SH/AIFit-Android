package com.jlsh.aifit.feature.nutrition.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.nutrition.data.dto.AnalyzeMealFromTextRequestDto
import com.jlsh.aifit.feature.nutrition.domain.model.MealLog
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionLogRepository
import javax.inject.Inject

class AnalyzeMealFromTextUseCase @Inject constructor(
    private val repository: NutritionLogRepository,
) {
    suspend operator fun invoke(request: AnalyzeMealFromTextRequestDto): Result<MealLog> =
        repository.analyzeMealFromText(request)
}

