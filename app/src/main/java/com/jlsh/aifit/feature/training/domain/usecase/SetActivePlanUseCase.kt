package com.jlsh.aifit.feature.training.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import com.jlsh.aifit.feature.training.domain.repository.TrainingRepository
import javax.inject.Inject

/**
 * Caso de uso para marcar un plan como activo y pausar el plan activo anterior.
 */
class SetActivePlanUseCase @Inject constructor(
    private val repository: TrainingRepository,
) {
    /**
     * Activa el plan indicado en servidor y caché local.
     *
     * @param planId Identificador del plan a activar.
     * @return [Result.Success] con el plan activado, o [Result.Error] (p. ej. plan no encontrado).
     */
    suspend operator fun invoke(planId: String): Result<TrainingPlan> =
        repository.activatePlan(planId)
}
