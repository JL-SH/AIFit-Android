package com.jlsh.aifit.feature.workout.data.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.feature.workout.data.api.WorkoutApiService
import com.jlsh.aifit.feature.workout.data.dto.LogWorkoutSessionRequestDto
import com.jlsh.aifit.feature.workout.data.local.WorkoutLogDao
import com.jlsh.aifit.feature.workout.data.mapper.WorkoutMapper.toDomain
import com.jlsh.aifit.feature.workout.data.mapper.WorkoutMapper.toEntity
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import com.jlsh.aifit.feature.workout.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class WorkoutRepositoryImpl @Inject constructor(
    private val apiService: WorkoutApiService,
    private val dao: WorkoutLogDao,
) : BaseRemoteDataSource(), WorkoutRepository {

    override suspend fun logSession(request: LogWorkoutSessionRequestDto): Result<WorkoutLog> {
        return when (val remote = safeApiCall { apiService.logWorkoutSession(request) }) {
            is Result.Success -> {
                val log = remote.data.toDomain()
                dao.upsertAll(listOf(log.toEntity()))
                Result.Success(log)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    override fun getHistory(
        planId: String?,
        from: String?,
        to: String?,
    ): Flow<Result<List<WorkoutLog>>> = flow {
        emit(Result.Loading)

        val cached = dao.getAll().map { it.toDomain() }
        if (cached.isNotEmpty()) {
            emit(Result.Success(cached))
        }

        when (val remote = safeApiCall { apiService.getWorkoutLogs(planId, from, to) }) {
            is Result.Success -> {
                val logs = remote.data.map { it.toDomain() }
                dao.upsertAll(logs.map { it.toEntity() })
                emit(Result.Success(logs))
            }
            is Result.Error -> {
                if (cached.isEmpty()) emit(remote)
            }
            else -> Unit
        }
    }

    override suspend fun getLogDetail(id: String): Result<WorkoutLog> {
        return when (val remote = safeApiCall { apiService.getWorkoutLogById(id) }) {
            is Result.Success -> Result.Success(remote.data.toDomain())
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    override suspend fun deleteLog(id: String): Result<Unit> {
        return when (val remote = safeApiCall { apiService.deleteWorkoutLog(id) }) {
            is Result.Success -> {
                dao.deleteById(id)
                Result.Success(Unit)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }
}

