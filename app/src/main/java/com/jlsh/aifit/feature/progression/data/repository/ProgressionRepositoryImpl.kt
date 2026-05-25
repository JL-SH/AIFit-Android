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
 * Remote deployment of [ProgressionRepository] for load and volume recommendations.
 *
 * @param apiService Progression HTTP Client.
 */
class ProgressionRepositoryImpl @Inject constructor(
    private val apiService: ProgressionApiService,
) : BaseRemoteDataSource(), ProgressionRepository {

    /**
     * @param exerciseId Exercise identifier.
     * @return [Result.Success] with the exercise recommendation, or [Result.Error] on network failure.
     */
    override suspend fun getExerciseRecommendation(exerciseId: String): Result<ProgressionRecommendation> =
        when (val r = safeApiCall { apiService.getExerciseRecommendation(exerciseId) }) {
            is Result.Success -> Result.Success(r.data.toDomain())
            is Result.Error -> r
            else -> Result.Loading
        }

    /**
     * @param planId Identifier of the training plan.
     * @return [Result.Success] with plan summary, or [Result.Error] on network failure.
     */
    override suspend fun getPlanRecommendations(planId: String): Result<PlanProgressionSummary> =
        when (val r = safeApiCall { apiService.getPlanRecommendations(planId) }) {
            is Result.Success -> Result.Success(r.data.toDomain())
            is Result.Error -> r
            else -> Result.Loading
        }
}

