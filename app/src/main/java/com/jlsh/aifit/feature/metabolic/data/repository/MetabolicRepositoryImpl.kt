package com.jlsh.aifit.feature.metabolic.data.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.feature.metabolic.data.api.MetabolicApiService
import com.jlsh.aifit.feature.metabolic.data.dto.ApplyMetabolicAdjustmentRequestDto
import com.jlsh.aifit.feature.metabolic.data.mapper.MetabolicMapper.toDomain
import com.jlsh.aifit.feature.metabolic.domain.model.MetabolicAnalysis
import com.jlsh.aifit.feature.metabolic.domain.model.MetabolicInsight
import com.jlsh.aifit.feature.metabolic.domain.repository.MetabolicRepository
import com.jlsh.aifit.feature.nutrition.data.mapper.NutritionMapper.toDomain
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget
import javax.inject.Inject

/**
 * Implementación remota de [MetabolicRepository] sobre la API metabólica.
 *
 * @param apiService Cliente HTTP de análisis, insights y ajustes.
 */
class MetabolicRepositoryImpl @Inject constructor(
    private val apiService: MetabolicApiService,
) : BaseRemoteDataSource(), MetabolicRepository {

    /**
     * @return [Result.Success] con el análisis metabólico, o [Result.Error] si la petición falla.
     */
    override suspend fun analyzeMetabolicProgress(): Result<MetabolicAnalysis> =
        when (val r = safeApiCall { apiService.getAnalysis() }) {
            is Result.Success -> Result.Success(r.data.toDomain())
            is Result.Error -> r
            else -> Result.Loading
        }

    /**
     * @return [Result.Success] con el historial de insights, o [Result.Error] en fallo de red.
     */
    override suspend fun getInsights(): Result<List<MetabolicInsight>> =
        when (val r = safeApiCall { apiService.getInsights() }) {
            is Result.Success -> Result.Success(r.data.map { it.toDomain() })
            is Result.Error -> r
            else -> Result.Loading
        }

    /**
     * @param request Objetivos calóricos y de macros sugeridos por el análisis.
     * @return [Result.Success] con el [NutritionTarget] actualizado, o [Result.Error] si no se aplica.
     */
    override suspend fun applyAdjustment(request: ApplyMetabolicAdjustmentRequestDto): Result<NutritionTarget> =
        when (val r = safeApiCall { apiService.applyAdjustment(request) }) {
            is Result.Success -> Result.Success(r.data.toDomain())
            is Result.Error -> r
            else -> Result.Loading
        }
}

