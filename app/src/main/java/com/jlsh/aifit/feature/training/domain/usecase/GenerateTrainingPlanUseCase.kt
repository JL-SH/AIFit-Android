package com.jlsh.aifit.feature.training.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.training.data.dto.GenerateAdaptiveTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.data.dto.GenerateTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan
import com.jlsh.aifit.feature.training.domain.repository.TrainingRepository
import javax.inject.Inject

/**
 * Caso de uso para generar planes de entrenamiento estándar o adaptativos vía backend.
 */
class GenerateTrainingPlanUseCase @Inject constructor(
    private val repository: TrainingRepository,
) {
    /**
     * Genera un plan de entrenamiento a partir de los parámetros del cuestionario inicial.
     *
     * @param request Parámetros de generación (frecuencia, objetivo, nivel, etc.).
     * @return [Result.Success] con el plan creado, o [Result.Error] si falla la red o la sesión.
     */
    suspend operator fun invoke(request: GenerateTrainingPlanRequestDto): Result<TrainingPlan> =
        repository.generateTrainingPlan(request)

    /**
     * Genera un plan adaptativo que tiene en cuenta historial y feedback del usuario.
     *
     * @param request Parámetros adaptativos, incluyendo consideraciones e historial opcional.
     * @return [Result.Success] con el plan generado, o [Result.Error] en caso de fallo.
     */
    suspend fun invokeAdaptive(request: GenerateAdaptiveTrainingPlanRequestDto): Result<TrainingPlan> =
        repository.generateAdaptiveTrainingPlan(request)
}
