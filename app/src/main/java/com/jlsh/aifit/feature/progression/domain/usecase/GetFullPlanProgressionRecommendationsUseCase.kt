package com.jlsh.aifit.feature.progression.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progression.domain.model.PlanProgressionSummary
import com.jlsh.aifit.feature.progression.domain.repository.ProgressionRepository
import javax.inject.Inject

/**
 * Caso de uso que obtiene recomendaciones de progresión para todos los ejercicios de un plan.
 *
 * @param repository Repositorio de progresión de entrenamiento.
 */
class GetFullPlanProgressionRecommendationsUseCase @Inject constructor(
    private val repository: ProgressionRepository,
) {
    /**
     * Carga el resumen de progresión del plan completo.
     *
     * @param planId Identificador del plan de entrenamiento.
     * @return [Result.Success] con [PlanProgressionSummary], o [Result.Error] si falla la consulta.
     */
    suspend operator fun invoke(planId: String): Result<PlanProgressionSummary> =
        repository.getPlanRecommendations(planId)
}

