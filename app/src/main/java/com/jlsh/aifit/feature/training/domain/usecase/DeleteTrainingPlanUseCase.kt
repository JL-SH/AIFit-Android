package com.jlsh.aifit.feature.training.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.training.domain.repository.TrainingRepository
import javax.inject.Inject

class DeleteTrainingPlanUseCase @Inject constructor(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(planId: String): Result<Unit> =
        repository.deleteTrainingPlan(planId)
}

