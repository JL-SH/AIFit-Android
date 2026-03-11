package com.jlsh.aifit.feature.diet.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.diet.data.dto.GenerateAdaptiveDietPlanRequestDto
import com.jlsh.aifit.feature.diet.data.dto.GenerateDietPlanRequestDto
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.repository.DietRepository
import javax.inject.Inject

class GenerateDietPlanUseCase @Inject constructor(
    private val repository: DietRepository,
) {
    suspend operator fun invoke(request: GenerateDietPlanRequestDto): Result<DietPlan> =
        repository.generateDietPlan(request)

    suspend fun invokeAdaptive(request: GenerateAdaptiveDietPlanRequestDto): Result<DietPlan> =
        repository.generateAdaptiveDietPlan(request)
}

