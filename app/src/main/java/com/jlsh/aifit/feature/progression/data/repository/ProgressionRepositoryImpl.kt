package com.jlsh.aifit.feature.progression.data.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.feature.progression.data.api.ProgressionApiService
import com.jlsh.aifit.feature.progression.data.mapper.ProgressionMapper.toDomain
import com.jlsh.aifit.feature.progression.domain.model.PlanProgressionSummary
import com.jlsh.aifit.feature.progression.domain.model.ProgressionRecommendation
import com.jlsh.aifit.feature.progression.domain.repository.ProgressionRepository
import javax.inject.Inject

/**
 * Implementación remota de [ProgressionRepository] para recomendaciones de carga y volumen.
 *
 * @param apiService Cliente HTTP de progresión.
 */
class ProgressionRepositoryImpl @Inject constructor(
    private val apiService: ProgressionApiService,
) : BaseRemoteDataSource(), ProgressionRepository {

    /**
     * @param exerciseId Identificador del ejercicio.
     * @return [Result.Success] con la recomendación del ejercicio, o [Result.Error] en fallo de red.
     */
    override suspend fun getExerciseRecommendation(exerciseId: String): Result<ProgressionRecommendation> =
        when (val r = safeApiCall { apiService.getExerciseRecommendation(exerciseId) }) {
            is Result.Success -> Result.Success(r.data.toDomain())
            is Result.Error -> r
            else -> Result.Loading
        }

    /**
     * @param planId Identificador del plan de entrenamiento.
     * @return [Result.Success] con el resumen del plan, o [Result.Error] en fallo de red.
     */
    override suspend fun getPlanRecommendations(planId: String): Result<PlanProgressionSummary> =
        when (val r = safeApiCall { apiService.getPlanRecommendations(planId) }) {
            is Result.Success -> Result.Success(r.data.toDomain())
            is Result.Error -> r
            else -> Result.Loading
        }
}

