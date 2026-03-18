package com.jlsh.aifit.feature.training.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import com.jlsh.aifit.feature.training.domain.repository.TrainingRepository
import javax.inject.Inject

class SetActivePlanUseCase @Inject constructor(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(planId: String): Result<TrainingPlan> =
        repository.activatePlan(planId)
}

