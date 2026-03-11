package com.jlsh.aifit.feature.nutrition.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionTargetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentNutritionTargetUseCase @Inject constructor(
    private val repository: NutritionTargetRepository,
) {
    operator fun invoke(): Flow<Result<NutritionTarget>> =
        repository.getCurrentTarget()
}

