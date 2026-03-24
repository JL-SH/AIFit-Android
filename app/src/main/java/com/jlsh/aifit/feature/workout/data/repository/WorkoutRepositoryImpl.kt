package com.jlsh.aifit.feature.workout.data.repository

import android.util.Log
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.feature.workout.data.api.WorkoutApiService
import com.jlsh.aifit.feature.workout.data.dto.FinalizeWorkoutSessionRequestDto
import com.jlsh.aifit.feature.workout.data.dto.LogWorkoutSessionRequestDto
import com.jlsh.aifit.feature.workout.data.dto.LogWorkoutSetRequestDto
import com.jlsh.aifit.feature.workout.data.local.WorkoutLogDao
import com.jlsh.aifit.feature.workout.data.mapper.WorkoutMapper.toDomain
import com.jlsh.aifit.feature.workout.data.mapper.WorkoutMapper.toDto
import com.jlsh.aifit.feature.workout.data.mapper.WorkoutMapper.toEntity
import com.jlsh.aifit.feature.workout.domain.model.JointPainEntry
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import com.jlsh.aifit.feature.workout.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
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

    override suspend fun addSetToLog(logId: String, set: LogWorkoutSetRequestDto): Result<Unit> {
        return when (val remote = safeApiCall { apiService.addSetToLog(logId, set) }) {
            is Result.Success -> Result.Success(Unit)
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

        val fromEpochDay = from?.let { LocalDate.parse(it).toEpochDay() }
        val toEpochDay = to?.let { LocalDate.parse(it).toEpochDay() }
        val cached = dao.getAll()
            .filter { entity ->
                (fromEpochDay == null || entity.date >= fromEpochDay) &&
                    (toEpochDay == null || entity.date <= toEpochDay) &&
                    (planId == null || entity.trainingPlanId == planId)
            }
            .map { it.toDomain() }
        // TODO: remove diagnostic log below
        Log.d("AIFIT_REPO", "getHistory cache emission — count=${cached.size}, logs=${cached.map { "id=${it.id} isLocked=${it.isLocked} date=${it.date}" }}")
        if (cached.isNotEmpty()) {
            emit(Result.Success(cached))
        }

        when (val remote = safeApiCall { apiService.getWorkoutLogs(planId, from, to) }) {
            is Result.Success -> {
                val logs = remote.data.map { it.toDomain() }
                // TODO: remove diagnostic log below
                Log.d("AIFIT_REPO", "getHistory network emission — count=${logs.size}, logs=${logs.map { "id=${it.id} isLocked=${it.isLocked} date=${it.date}" }}")
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

    override suspend fun finalizeWorkoutSession(
        logId: String,
        systemicFatigue: Int,
        jointPainReport: List<JointPainEntry>,
    ): Result<WorkoutLog> {
        // TODO: remove diagnostic logs below
        Log.d("AIFIT_REPO", "finalizeWorkoutSession called — logId=$logId")
        val request = FinalizeWorkoutSessionRequestDto(
            systemicFatigue = systemicFatigue,
            jointPainReport = jointPainReport.map { it.toDto() },
        )
        return when (val remote = safeApiCall { apiService.finalizeWorkoutSession(logId, request) }) {
            is Result.Success -> {
                Log.d("AIFIT_REPO", "finalize API success — raw isLocked=${remote.data.isLocked} (from DTO)")
                val log = remote.data.toDomain()
                dao.upsertAll(listOf(log.toEntity()))
                Log.d("AIFIT_REPO", "dao.upsertAll() called — saved isLocked=${log.isLocked}")
                Result.Success(log)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    override suspend fun getPreviousSessionForDay(
        planId: String,
        dayId: String,
    ): Result<WorkoutLog?> {
        return when (val remote = safeApiCall { apiService.getWorkoutLogs(planId = planId, dayId = dayId) }) {
            is Result.Success -> Result.Success(remote.data.firstOrNull()?.toDomain())
            is Result.Error -> remote
            else -> Result.Loading
        }
    }
}

