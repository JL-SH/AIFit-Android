package com.jlsh.aifit.feature.diet.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.diet.data.dto.GenerateAdaptiveDietPlanRequestDto
import com.jlsh.aifit.feature.diet.data.dto.GenerateDietPlanRequestDto
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.repository.DietRepository
import javax.inject.Inject

/**
 * Use case that generates a new, standard or adaptive diet plan based on the user profile.
 *
 * @param repository Diet plan repository.
 */
class GenerateDietPlanUseCase @Inject constructor(
    private val repository: DietRepository,
) {
    /**
     * Generates a diet plan based on explicit user parameters.
     *
     * @param request Spawn parameters (duration, meals per day, preference, etc.).
     * @return [Result.Success] with the [DietPlan] created, or [Result.Error] if the generation fails.
     */
    suspend operator fun invoke(request: GenerateDietPlanRequestDto): Result<DietPlan> =
        repository.generateDietPlan(request)

    /**
     * Generates an adaptive plan that takes into account the user's nutritional history and profile.
     *
     * @param request Adaptive parameters (goal, considerations, history, etc.).
     * @return [Result.Success] with the generated [DietPlan], or [Result.Error] if the generation fails.
     */
    suspend fun invokeAdaptive(request: GenerateAdaptiveDietPlanRequestDto): Result<DietPlan> =
        repository.generateAdaptiveDietPlan(request)
}
