package com.jlsh.aifit.feature.home.data.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.feature.home.data.api.HomeApiService
import com.jlsh.aifit.feature.home.data.mapper.HomeBootstrapMapper.toDomain
import com.jlsh.aifit.feature.home.domain.model.HomeBootstrap
import com.jlsh.aifit.feature.home.domain.repository.HomeRepository
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val apiService: HomeApiService,
) : BaseRemoteDataSource(), HomeRepository {

    override suspend fun getBootstrap(): Result<HomeBootstrap> =
        when (val remote = safeApiCall { apiService.getBootstrap() }) {
            is Result.Success -> Result.Success(remote.data.toDomain())
            is Result.Error -> remote
            else -> Result.Loading
        }
}
