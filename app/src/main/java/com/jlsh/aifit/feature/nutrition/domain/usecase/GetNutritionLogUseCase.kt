package com.jlsh.aifit.feature.nutrition.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionLog
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionLogRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetNutritionLogUseCase @Inject constructor(
    private val repository: NutritionLogRepository,
) {
    operator fun invoke(date: LocalDate): Flow<Result<NutritionLog>> =
        repository.getNutritionLog(date)
}

