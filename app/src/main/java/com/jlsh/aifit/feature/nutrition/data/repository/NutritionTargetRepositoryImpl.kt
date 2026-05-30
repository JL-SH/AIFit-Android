package com.jlsh.aifit.feature.nutrition.data.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.feature.nutrition.data.api.NutritionTargetApiService
import com.jlsh.aifit.feature.nutrition.data.dto.UpdateNutritionTargetRequestDto
import com.jlsh.aifit.feature.nutrition.data.local.NutritionTargetDao
import com.jlsh.aifit.feature.nutrition.data.mapper.NutritionMapper.toDomain
import com.jlsh.aifit.feature.nutrition.data.mapper.NutritionMapper.toEntity
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionTargetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NutritionTargetRepositoryImpl @Inject constructor(
    private val apiService: NutritionTargetApiService,
    private val dao: NutritionTargetDao,
) : BaseRemoteDataSource(), NutritionTargetRepository {

    override fun getCurrentTarget(): Flow<Result<NutritionTarget>> = flow {
        emit(Result.Loading)

        val cached = withContext(Dispatchers.IO) { dao.getCurrent() }
        if (cached != null) {
            emit(Result.Success(cached.toDomain()))
        }

        when (val remote = safeApiCall { apiService.getCurrentTarget() }) {
            is Result.Success -> {
                val target = remote.data.toDomain()
                dao.upsert(target.toEntity())
                emit(Result.Success(target))
            }
            is Result.Error -> {
                if (cached == null) emit(remote)
            }
            else -> Unit
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun updateTarget(request: UpdateNutritionTargetRequestDto): Result<NutritionTarget> {
        return when (val remote = safeApiCall { apiService.updateTarget(request) }) {
            is Result.Success -> {
                val target = remote.data.toDomain()
                dao.upsert(target.toEntity())
                Result.Success(target)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }
}
