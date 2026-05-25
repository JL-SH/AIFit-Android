package com.jlsh.aifit.feature.training.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.training.data.dto.GenerateAdaptiveTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.data.dto.GenerateTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import com.jlsh.aifit.feature.training.domain.repository.TrainingRepository
import javax.inject.Inject

/**
 * Use case to generate standard or adaptive training plans via backend.
 */
class GenerateTrainingPlanUseCase @Inject constructor(
    private val repository: TrainingRepository,
) {
    /**
     * Generate a training plan based on the parameters of the initial questionnaire.
     *
     * @param request Build parameters (frequency, target, level, etc.).
     * @return [Result.Success] with the created plan, or [Result.Error] if the network or session fails.
     */
    suspend operator fun invoke(request: GenerateTrainingPlanRequestDto): Result<TrainingPlan> =
        repository.generateTrainingPlan(request)

    /**
     * Generates an adaptive plan that takes into account user history and feedback.
     *
     * @param request Adaptive parameters, including considerations and optional history.
     * @return [Result.Success] with the generated plan, or [Result.Error] on failure.
     */
    suspend fun invokeAdaptive(request: GenerateAdaptiveTrainingPlanRequestDto): Result<TrainingPlan> =
        repository.generateAdaptiveTrainingPlan(request)
}
