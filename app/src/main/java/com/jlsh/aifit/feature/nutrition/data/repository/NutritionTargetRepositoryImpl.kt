package com.jlsh.aifit.feature.nutrition.data.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.feature.nutrition.data.api.NutritionTargetApiService
import com.jlsh.aifit.feature.nutrition.data.dto.UpdateNutritionTargetRequestDto
import com.jlsh.aifit.feature.nutrition.data.mapper.NutritionMapper.toDomain
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionTargetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class NutritionTargetRepositoryImpl @Inject constructor(
    private val apiService: NutritionTargetApiService,
) : BaseRemoteDataSource(), NutritionTargetRepository {

    override fun getCurrentTarget(): Flow<Result<NutritionTarget>> = flow {
        emit(Result.Loading)
        when (val remote = safeApiCall { apiService.getCurrentTarget() }) {
            is Result.Success -> emit(Result.Success(remote.data.toDomain()))
            is Result.Error -> emit(remote)
            else -> Unit
        }
    }

    override suspend fun updateTarget(request: UpdateNutritionTargetRequestDto): Result<NutritionTarget> {
        return when (val remote = safeApiCall { apiService.updateTarget(request) }) {
            is Result.Success -> Result.Success(remote.data.toDomain())
            is Result.Error -> remote
            else -> Result.Loading
        }
    }
}

