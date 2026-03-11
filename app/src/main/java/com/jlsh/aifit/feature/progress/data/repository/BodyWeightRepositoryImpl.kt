package com.jlsh.aifit.feature.progress.data.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.feature.progress.data.api.BodyWeightApiService
import com.jlsh.aifit.feature.progress.data.dto.LogBodyWeightRequestDto
import com.jlsh.aifit.feature.progress.data.local.BodyWeightDao
import com.jlsh.aifit.feature.progress.data.mapper.ProgressMapper.toDomain
import com.jlsh.aifit.feature.progress.data.mapper.ProgressMapper.toEntity
import com.jlsh.aifit.feature.progress.domain.model.BodyWeightLog
import com.jlsh.aifit.feature.progress.domain.repository.BodyWeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class BodyWeightRepositoryImpl @Inject constructor(
    private val apiService: BodyWeightApiService,
    private val dao: BodyWeightDao,
) : BaseRemoteDataSource(), BodyWeightRepository {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun getHistory(from: String, to: String): Flow<Result<List<BodyWeightLog>>> = flow {
        emit(Result.Loading)

        val fromEpoch = runCatching { LocalDate.parse(from, dateFormatter).toEpochDay() }.getOrDefault(0L)
        val toEpoch = runCatching { LocalDate.parse(to, dateFormatter).toEpochDay() }.getOrDefault(Long.MAX_VALUE)
        val cached = dao.getByDateRange(fromEpoch, toEpoch)
        if (cached.isNotEmpty()) {
            emit(Result.Success(cached.map { it.toDomain() }))
        }

        when (val remote = safeApiCall { apiService.getHistory(from, to) }) {
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

    override suspend fun logWeight(request: LogBodyWeightRequestDto): Result<BodyWeightLog> {
        return when (val remote = safeApiCall { apiService.logWeight(request) }) {
            is Result.Success -> {
                val log = remote.data.toDomain()
                dao.upsert(log.toEntity())
                Result.Success(log)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }
}

