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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Implementation of [BodyWeightRepository] with reactive cache in Room and remote synchronization.
 *
 * Emits local data first and refreshes in the background from the API; Room inserts
 * automatically rebroadcast to the observer.
 *
 * @param apiService Bodyweight HTTP Client.
 * @param dao Local access to weight history.
 */
class BodyWeightRepositoryImpl @Inject constructor(
    private val apiService: BodyWeightApiService,
    private val dao: BodyWeightDao,
) : BaseRemoteDataSource(), BodyWeightRepository {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * Observe the weight history in the date range, combining local and network cache.
     *
     * @param from Start date in local ISO format (`yyyy-MM-dd`).
     * @param to End date in local ISO format (`yyyy-MM-dd`).
     * @return Flow that emits [Result.Loading], then [Result.Success] with the list (or [Result.Error] on non-cache network failures).
     */
    override fun getHistory(from: String, to: String): Flow<Result<List<BodyWeightLog>>> {
        val fromEpoch = runCatching { LocalDate.parse(from, dateFormatter).toEpochDay() }.getOrDefault(0L)
        val toEpoch = runCatching { LocalDate.parse(to, dateFormatter).toEpochDay() }.getOrDefault(Long.MAX_VALUE)

        return flow {
            emit(Result.Loading)
            val cached = dao.observeByDateRange(fromEpoch, toEpoch).first().map { it.toDomain() }
            emit(Result.Success(cached))
            when (val remote = safeApiCall { apiService.getHistory(from, to) }) {
                is Result.Success -> {
                    val logs = remote.data.map { it.toDomain() }
                    dao.upsertAll(logs.map { it.toEntity() })
                }
                is Result.Error -> {
                    // Keep cache-only behavior in offline errors.
                }
                else -> Unit
            }
        }
    }

    /**
     * Registers a weight on the server and persists it in Room.
     *
     * @param request Weight, date and notes of the record.
     * @return [Result.Success] with the log created, [Result.Error] on API failure, or [Result.Loading] transient.
     */
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

