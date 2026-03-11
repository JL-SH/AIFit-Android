package com.jlsh.aifit.feature.nutrition.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionLog
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionLogRepository
import javax.inject.Inject

class GetNutritionHistoryUseCase @Inject constructor(
    private val repository: NutritionLogRepository,
) {
    suspend operator fun invoke(from: String, to: String): Result<List<NutritionLog>> =
        repository.getNutritionHistory(from, to)
}

