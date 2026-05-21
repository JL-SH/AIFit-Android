package com.jlsh.aifit.feature.diet.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.diet.data.dto.GenerateAdaptiveDietPlanRequestDto
import com.jlsh.aifit.feature.diet.data.dto.GenerateDietPlanRequestDto
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.repository.DietRepository
import javax.inject.Inject

/**
 * Caso de uso que genera un plan de dieta nuevo, estándar o adaptativo según el perfil del usuario.
 *
 * @param repository Repositorio de planes de dieta.
 */
class GenerateDietPlanUseCase @Inject constructor(
    private val repository: DietRepository,
) {
    /**
     * Genera un plan de dieta a partir de parámetros explícitos del usuario.
     *
     * @param request Parámetros de generación (duración, comidas por día, preferencia, etc.).
     * @return [Result.Success] con el [DietPlan] creado, o [Result.Error] si falla la generación.
     */
    suspend operator fun invoke(request: GenerateDietPlanRequestDto): Result<DietPlan> =
        repository.generateDietPlan(request)

    /**
     * Genera un plan adaptativo que tiene en cuenta el historial nutricional y el perfil del usuario.
     *
     * @param request Parámetros adaptativos (objetivo, consideraciones, historial, etc.).
     * @return [Result.Success] con el [DietPlan] generado, o [Result.Error] si falla la generación.
     */
    suspend fun invokeAdaptive(request: GenerateAdaptiveDietPlanRequestDto): Result<DietPlan> =
        repository.generateAdaptiveDietPlan(request)
}
