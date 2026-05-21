package com.jlsh.aifit.feature.training.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.training.domain.repository.TrainingRepository
import javax.inject.Inject

/**
 * Caso de uso para eliminar un plan de entrenamiento del servidor y la caché local.
 */
class DeleteTrainingPlanUseCase @Inject constructor(
    private val repository: TrainingRepository,
) {
    /**
     * Elimina el plan identificado por [planId].
     *
     * @param planId Identificador del plan a eliminar.
     * @return [Result.Success] si la eliminación se confirma, o [Result.Error] si falla.
     */
    suspend operator fun invoke(planId: String): Result<Unit> =
        repository.deleteTrainingPlan(planId)
}
