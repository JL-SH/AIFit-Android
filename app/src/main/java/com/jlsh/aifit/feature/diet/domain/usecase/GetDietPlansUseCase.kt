package com.jlsh.aifit.feature.diet.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.repository.DietRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDietPlansUseCase @Inject constructor(
    private val repository: DietRepository,
) {
    operator fun invoke(): Flow<Result<List<DietPlan>>> =
        repository.getDietPlans()
}

