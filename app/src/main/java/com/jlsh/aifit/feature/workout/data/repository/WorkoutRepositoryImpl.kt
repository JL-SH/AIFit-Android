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
import com.jlsh.aifit.feature.workout.domain.WorkoutHistoryNotifier
import com.jlsh.aifit.feature.workout.domain.model.ExerciseProgressionHistory
import com.jlsh.aifit.feature.workout.domain.model.JointPainEntry
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import com.jlsh.aifit.feature.workout.domain.repository.WorkoutRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Collections
import javax.inject.Inject

/**
 * Implementation of [WorkoutRepository] with Room cache and re-insertion protection
 * of deleted logs during stale network responses.
 */
class WorkoutRepositoryImpl @Inject constructor(
    private val apiService: WorkoutApiService,
    private val dao: WorkoutLogDao,
    private val workoutHistoryNotifier: WorkoutHistoryNotifier,
) : BaseRemoteDataSource(), WorkoutRepository {

    /** IDs whose delete API call is still in-flight.
     *  While an ID is in this set, [getHistory] will filter it out so
     *  a concurrent network response cannot re-insert the deleted row. */
    private val pendingDeleteIds: MutableSet<String> =
        Collections.synchronizedSet(mutableSetOf())

    /**
     * Incremented every time a delete is initiated.
     * Each [getHistory] call captures this value at start; if the value changes
     * before the network response is processed, the response is stale (it was
     * sent before the delete) and is discarded entirely — preventing re-insertion
     * of the deleted row even across different ViewModel instances.
     */
    @Volatile
    private var deleteGeneration: Int = 0

    /**
     * Creates a new session log on the server and saves it in Room.
     *
     * @param request Initial session data (plan, day, date, optional series).
     */
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

    /**
     * Add a series to an existing log via API.
     *
     * @param logId Identifier of the session log.
     * @param set Data of the series to register.
     */
    override suspend fun addSetToLog(logId: String, set: LogWorkoutSetRequestDto): Result<Unit> {
        return when (val remote = safeApiCall { apiService.addSetToLog(logId, set) }) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    /**
     * Issue log history: leaked cache and then network reconciliation.
     *
     * @param planId Filter by plan; null includes all.
     * @param from ISO start date optional.
     * @param to Optional ISO End Date.
     */
    override fun getHistory(
        planId: String?,
        from: String?,
        to: String?,
    ): Flow<Result<List<WorkoutLog>>> = flow {
        emit(Result.Loading)

        // Snapshot the generation BEFORE the network call so we can detect if a
        // delete was triggered while we were waiting for the response.
        val generationAtStart = deleteGeneration

        val fromEpochDay = from?.let { LocalDate.parse(it).toEpochDay() }
        val toEpochDay = to?.let { LocalDate.parse(it).toEpochDay() }
        val cached = withContext(Dispatchers.IO) {
            val entities = when {
                fromEpochDay != null && toEpochDay != null ->
                    dao.getByDateRange(fromEpochDay, toEpochDay)
                else ->
                    dao.getAll()
            }
            entities
                .filter { entity ->
                    entity.id !in pendingDeleteIds &&
                        (planId == null || entity.trainingPlanId == planId)
                }
                .map { it.toDomain() }
        }
        // TODO: remove diagnostic log below
        Log.d("AIFIT_REPO", "getHistory cache emission — count=${cached.size}, logs=${cached.map { "id=${it.id} isLocked=${it.isLocked} date=${it.date}" }}")
        if (cached.isNotEmpty()) {
            emit(Result.Success(cached))
        }

        when (val remote = safeApiCall { apiService.getWorkoutLogs(planId, from, to) }) {
            is Result.Success -> {
                // If the generation changed while we were waiting for the network response,
                // a delete was initiated after this GET request was sent.  The response is
                // therefore stale (it was captured before the delete) and MUST be discarded
                // to prevent re-inserting the deleted row into Room.
                if (deleteGeneration != generationAtStart) {
                    Log.d("AIFIT_REPO", "getHistory: discarding stale network response (generation changed $generationAtStart → $deleteGeneration)")
                    return@flow
                }

                // ─── FIX: do NOT call pendingDeleteIds.removeAll { it !in serverIds } ───
                //
                // That line caused a race condition with HomeViewModel.loadTodayWorkoutHistory(),
                // which calls getHistory(from = today, to = today).  A date-filtered response
                // never contains workouts from other days, so removeAll treated every past-day
                // ID as "server-confirmed deleted" and wiped it from pendingDeleteIds.  A
                // concurrent unfiltered getHistory() call (from WorkoutHistoryScreen) then
                // received a stale response still containing the deleted item; finding the ID
                // absent from pendingDeleteIds, it passed the filter and re-inserted the row
                // into Room — making the deleted session reappear.
                //
                // IDs in pendingDeleteIds are intentionally kept until process restart (they
                // accumulate but are few and negligible in memory).  Once the server fully
                // propagates the delete, future responses stop returning the ID, so the filter
                // below becomes a no-op for old deleted IDs.

                val logs = remote.data.map { it.toDomain() }
                    .filter { it.id !in pendingDeleteIds }
                Log.d("AIFIT_REPO", "getHistory network emission — count=${logs.size}, pendingDeletes=${pendingDeleteIds.size}")

                // Only upsert items NOT pending deletion; then evict any that a racing
                // upsert may have already written.
                dao.upsertAll(logs.map { it.toEntity() })
                pendingDeleteIds.forEach { pendingId -> dao.deleteById(pendingId) }
                emit(Result.Success(logs))
            }
            is Result.Error -> {
                if (cached.isEmpty()) emit(remote)
            }
            else -> Unit
        }
    }

    override suspend fun getCachedWorkoutLogs(
        planId: String?,
        from: String?,
        to: String?,
    ): List<WorkoutLog> = withContext(Dispatchers.IO) {
        val fromEpochDay = from?.let { LocalDate.parse(it).toEpochDay() }
        val toEpochDay = to?.let { LocalDate.parse(it).toEpochDay() }
        val entities = when {
            fromEpochDay != null && toEpochDay != null ->
                dao.getByDateRange(fromEpochDay, toEpochDay)
            else ->
                dao.getAll()
        }
        entities
            .filter { entity ->
                entity.id !in pendingDeleteIds &&
                    (planId == null || entity.trainingPlanId == planId)
            }
            .map { it.toDomain() }
    }

    /**
     * Obtains the details of a session log from the server.
     *
     * @param id Log identifier.
     */
    override suspend fun getLogDetail(id: String): Result<WorkoutLog> {
        return when (val remote = safeApiCall { apiService.getWorkoutLogById(id) }) {
            is Result.Success -> Result.Success(remote.data.toDomain())
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    /**
     * Delete a log locally and confirm with the server, with rollback on error.
     *
     * @param id Identifier of the log to delete.
     */
    override suspend fun deleteLog(id: String): Result<Unit> {
        // Increment the generation counter BEFORE adding to pendingDeleteIds.
        // Any getHistory() flow that captured an older generation will discard its
        // network response, preventing stale re-insertion of the deleted row.
        deleteGeneration++
        pendingDeleteIds.add(id)
        val backup = dao.getById(id)
        dao.deleteById(id)

        return when (val remote = safeApiCall { apiService.deleteWorkoutLog(id) }) {
            is Result.Success -> {
                pendingDeleteIds.remove(id)
                dao.deleteById(id) // ensure no racing upsert left the row behind
                Result.Success(Unit)
            }
            is Result.Error -> {
                pendingDeleteIds.remove(id)
                if (backup != null) dao.upsertAll(listOf(backup))
                remote
            }
            else -> Result.Loading
        }
    }

    /**
     * A session ends by blocking the log and fatigue and joint pain persist.
     *
     * @param logId Log identifier.
     * @param systemicFatigue Reported systemic fatigue.
     * @param jointPainReport Joint pain entries.
     */
    override suspend fun finalizeWorkoutSession(
        logId: String,
        systemicFatigue: Int,
        jointPainReport: List<JointPainEntry>,
    ): Result<WorkoutLog> {
        val backup = dao.getById(logId)
        val optimisticLog = backup?.toDomain()?.takeIf { !it.isLocked }?.copy(isLocked = true)
        if (optimisticLog != null) {
            dao.upsertAll(listOf(optimisticLog.toEntity()))
            workoutHistoryNotifier.notifyWorkoutFinalized(optimisticLog)
        }
        val request = FinalizeWorkoutSessionRequestDto(
            systemicFatigue = systemicFatigue,
            jointPainReport = jointPainReport.map { it.toDto() },
        )
        return when (val remote = safeApiCall { apiService.finalizeWorkoutSession(logId, request) }) {
            is Result.Success -> {
                val log = remote.data.toDomain()
                dao.upsertAll(listOf(log.toEntity()))
                workoutHistoryNotifier.notifyWorkoutFinalized(log)
                Result.Success(log)
            }
            is Result.Error -> {
                if (backup != null) {
                    dao.upsertAll(listOf(backup))
                    workoutHistoryNotifier.notifyWorkoutFinalized(backup.toDomain())
                }
                remote
            }
            else -> Result.Loading
        }
    }

    /**
     * Gets the last session recorded for a specific day of the plan (“phantom” series).
     *
     * @param planId Identifier of the training plan.
     * @param dayId Identifier of the training day.
     * @return [Result.Success] with the most recent log or null if there is no previous history.
     */
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

    /**
     * Looks up today's log for a plan day via API (bypasses [getHistory] pending-delete filter).
     * Loads full detail when a summary exists so callers receive persisted sets.
     */
    override suspend fun findOpenLogForDay(
        planId: String,
        dayId: String,
        date: String,
    ): Result<WorkoutLog?> {
        return when (
            val remote = safeApiCall {
                apiService.getWorkoutLogs(planId = planId, dayId = dayId, from = date, to = date)
            }
        ) {
            is Result.Success -> {
                val summary = remote.data.firstOrNull()
                    ?: return Result.Success(null)
                when (val detail = getLogDetail(summary.id)) {
                    is Result.Success -> Result.Success(detail.data)
                    is Result.Error -> detail
                    else -> Result.Loading
                }
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    override suspend fun getExerciseProgression(exerciseId: String): Result<ExerciseProgressionHistory> =
        when (val remote = safeApiCall { apiService.getExerciseProgression(exerciseId) }) {
            is Result.Success -> Result.Success(remote.data.toDomain())
            is Result.Error -> remote
            else -> Result.Loading
        }
}
