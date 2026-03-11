package com.jlsh.aifit.feature.progress.data.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.feature.progress.data.api.ProgressDashboardApiService
import com.jlsh.aifit.feature.progress.data.mapper.ProgressMapper.toDomain
import com.jlsh.aifit.feature.progress.domain.model.ProgressDashboard
import com.jlsh.aifit.feature.progress.domain.model.WeeklyProgressSummary
import com.jlsh.aifit.feature.progress.domain.repository.ProgressDashboardRepository
import javax.inject.Inject

class ProgressDashboardRepositoryImpl @Inject constructor(
    private val apiService: ProgressDashboardApiService,
) : BaseRemoteDataSource(), ProgressDashboardRepository {

    override suspend fun getDashboard(from: String, to: String): Result<ProgressDashboard> {
        return when (val remote = safeApiCall { apiService.getDashboard(from, to) }) {
            is Result.Success -> Result.Success(remote.data.toDomain())
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    override suspend fun getWeeklySummary(): Result<WeeklyProgressSummary> {
        return when (val remote = safeApiCall { apiService.getWeeklySummary() }) {
            is Result.Success -> Result.Success(remote.data.toDomain())
            is Result.Error -> remote
            else -> Result.Loading
        }
    }
}

