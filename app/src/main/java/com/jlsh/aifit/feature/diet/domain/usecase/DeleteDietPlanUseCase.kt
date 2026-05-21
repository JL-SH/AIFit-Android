package com.jlsh.aifit.feature.diet.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.diet.domain.repository.DietRepository
import javax.inject.Inject

/**
 * Caso de uso que elimina un plan de dieta del usuario en servidor y caché local.
 *
 * @param repository Repositorio de planes de dieta.
 */
class DeleteDietPlanUseCase @Inject constructor(
    private val repository: DietRepository,
) {
    /**
     * Elimina el plan identificado por [planId].
     *
     * @param planId Identificador del plan a eliminar.
     * @return [Result.Success] si la eliminación se confirma, o [Result.Error] si falla.
     */
    suspend operator fun invoke(planId: String): Result<Unit> =
        repository.deleteDietPlan(planId)
}
