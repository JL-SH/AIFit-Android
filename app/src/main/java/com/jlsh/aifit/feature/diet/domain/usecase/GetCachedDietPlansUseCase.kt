package com.jlsh.aifit.feature.diet.domain.usecase

import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.repository.DietRepository
import javax.inject.Inject

/** Reads diet plans from Room only (no network). */
class GetCachedDietPlansUseCase @Inject constructor(
    private val repository: DietRepository,
) {
    suspend operator fun invoke(): List<DietPlan> = repository.getCachedDietPlans()
}
