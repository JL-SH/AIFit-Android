package com.jlsh.aifit.feature.diet.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.repository.DietRepository
import javax.inject.Inject

class GetDietPlanDetailUseCase @Inject constructor(
    private val repository: DietRepository,
) {
    suspend operator fun invoke(planId: String): Result<DietPlan> =
        repository.getDietPlanDetail(planId)
}

