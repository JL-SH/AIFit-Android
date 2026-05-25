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
 * Remote implementation of [MetabolicRepository] over the metabolic API.
 *
 * @param apiService HTTP client for analysis, insights and adjustments.
 */
class MetabolicRepositoryImpl @Inject constructor(
    private val apiService: MetabolicApiService,
) : BaseRemoteDataSource(), MetabolicRepository {

    /**
     * @return [Result.Success] with metabolic analysis, or [Result.Error] if the request fails.
     */
    override suspend fun analyzeMetabolicProgress(): Result<MetabolicAnalysis> =
        when (val r = safeApiCall { apiService.getAnalysis() }) {
            is Result.Success -> Result.Success(r.data.toDomain())
            is Result.Error -> r
            else -> Result.Loading
        }

    /**
     * @return [Result.Success] with insight history, or [Result.Error] on network failure.
     */
    override suspend fun getInsights(): Result<List<MetabolicInsight>> =
        when (val r = safeApiCall { apiService.getInsights() }) {
            is Result.Success -> Result.Success(r.data.map { it.toDomain() })
            is Result.Error -> r
            else -> Result.Loading
        }

    /**
     * @param request Calorie and macro goals suggested by the analysis.
     * @return [Result.Success] with the updated [NutritionTarget], or [Result.Error] if not applicable.
     */
    override suspend fun applyAdjustment(request: ApplyMetabolicAdjustmentRequestDto): Result<NutritionTarget> =
        when (val r = safeApiCall { apiService.applyAdjustment(request) }) {
            is Result.Success -> Result.Success(r.data.toDomain())
            is Result.Error -> r
            else -> Result.Loading
        }
}

