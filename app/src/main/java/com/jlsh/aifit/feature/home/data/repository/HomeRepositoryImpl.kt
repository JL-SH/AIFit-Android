package com.jlsh.aifit.feature.home.data.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.core.session.SessionManager
import com.jlsh.aifit.feature.home.data.api.HomeApiService
import com.jlsh.aifit.feature.home.data.dto.HomeBootstrapResponseDto
import com.jlsh.aifit.feature.home.data.local.HomeBootstrapCacheDao
import com.jlsh.aifit.feature.home.data.local.HomeBootstrapCacheEntity
import com.jlsh.aifit.feature.home.data.mapper.HomeBootstrapMapper.toDomain
import com.jlsh.aifit.feature.home.domain.model.HomeBootstrap
import com.jlsh.aifit.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val apiService: HomeApiService,
    private val bootstrapCacheDao: HomeBootstrapCacheDao,
    private val sessionManager: SessionManager,
) : BaseRemoteDataSource(), HomeRepository {

    private val bootstrapJson = Json { ignoreUnknownKeys = true }

    override suspend fun getCachedBootstrap(): HomeBootstrap? = withContext(Dispatchers.IO) {
        val userId = sessionManager.getUserId() ?: return@withContext null
        val entity = bootstrapCacheDao.getByUserId(userId) ?: return@withContext null
        runCatching {
            bootstrapJson.decodeFromString<HomeBootstrapResponseDto>(entity.bootstrapJson).toDomain()
        }.getOrNull()
    }

    override suspend fun getBootstrap(): Result<HomeBootstrap> =
        when (val remote = safeApiCall { apiService.getBootstrap() }) {
            is Result.Success -> {
                val bootstrap = remote.data.toDomain()
                persistBootstrapCache(remote.data)
                Result.Success(bootstrap)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }

    private suspend fun persistBootstrapCache(dto: HomeBootstrapResponseDto) {
        val userId = sessionManager.getUserId() ?: return
        withContext(Dispatchers.IO) {
            bootstrapCacheDao.upsert(
                HomeBootstrapCacheEntity(
                    userId = userId,
                    bootstrapJson = bootstrapJson.encodeToString(dto),
                    cachedAt = System.currentTimeMillis(),
                ),
            )
        }
    }
}
