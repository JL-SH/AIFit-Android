package com.jlsh.aifit.feature.training.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.training.data.dto.GenerateAdaptiveTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.data.dto.GenerateTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import com.jlsh.aifit.feature.training.domain.repository.TrainingRepository
import javax.inject.Inject

class GenerateTrainingPlanUseCase @Inject constructor(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(request: GenerateTrainingPlanRequestDto): Result<TrainingPlan> =
        repository.generateTrainingPlan(request)

    suspend fun invokeAdaptive(request: GenerateAdaptiveTrainingPlanRequestDto): Result<TrainingPlan> =
        repository.generateAdaptiveTrainingPlan(request)
}

