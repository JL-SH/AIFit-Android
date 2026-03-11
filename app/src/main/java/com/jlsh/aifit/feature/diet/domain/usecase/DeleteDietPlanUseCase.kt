package com.jlsh.aifit.feature.diet.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.diet.domain.repository.DietRepository
import javax.inject.Inject

class DeleteDietPlanUseCase @Inject constructor(
    private val repository: DietRepository,
) {
    suspend operator fun invoke(planId: String): Result<Unit> =
        repository.deleteDietPlan(planId)
}

