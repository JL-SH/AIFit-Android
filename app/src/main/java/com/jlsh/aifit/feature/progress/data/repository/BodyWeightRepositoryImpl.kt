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
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class BodyWeightRepositoryImpl @Inject constructor(
    private val apiService: BodyWeightApiService,
    private val dao: BodyWeightDao,
) : BaseRemoteDataSource(), BodyWeightRepository {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun getHistory(from: String, to: String): Flow<Result<List<BodyWeightLog>>> {
        val fromEpoch = runCatching { LocalDate.parse(from, dateFormatter).toEpochDay() }.getOrDefault(0L)
        val toEpoch = runCatching { LocalDate.parse(to, dateFormatter).toEpochDay() }.getOrDefault(Long.MAX_VALUE)

        return channelFlow {
            send(Result.Loading)

            // Observe Room reactively — any insert/update automatically re-emits
            launch {
                dao.observeByDateRange(fromEpoch, toEpoch)
                    .map { entities -> Result.Success(entities.map { it.toDomain() }) as Result<List<BodyWeightLog>> }
                    .collect { send(it) }
            }

            // Trigger network refresh in a child coroutine — results go into Room,
            // which auto-triggers the observer above so the UI gets the latest data.
            launch {
                refreshFromNetwork(from, to)
            }
        }
    }

    /**
     * Fetches the latest data from the API and upserts into Room.
     * Called from within channelFlow so [safeApiCall] (from [BaseRemoteDataSource]) is accessible.
     */
    private suspend fun refreshFromNetwork(from: String, to: String) {
        when (val remote = safeApiCall { apiService.getHistory(from, to) }) {
            is Result.Success -> {
                val logs = remote.data.map { it.toDomain() }
                dao.upsertAll(logs.map { it.toEntity() })
            }
            is Result.Error -> {
                // Room observer already emitted cached data (if any).
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

