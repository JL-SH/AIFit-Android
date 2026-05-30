package com.jlsh.aifit.feature.workout.domain.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.workout.data.dto.LogWorkoutSessionRequestDto
import com.jlsh.aifit.feature.workout.data.dto.LogWorkoutSetRequestDto
import com.jlsh.aifit.feature.workout.domain.model.JointPainEntry
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    suspend fun logSession(request: LogWorkoutSessionRequestDto): Result<WorkoutLog>
    suspend fun addSetToLog(logId: String, set: LogWorkoutSetRequestDto): Result<Unit>
    fun getHistory(planId: String?, from: String?, to: String?): Flow<Result<List<WorkoutLog>>>
    suspend fun getCachedWorkoutLogs(
        planId: String? = null,
        from: String? = null,
        to: String? = null,
    ): List<WorkoutLog>
    suspend fun getLogDetail(id: String): Result<WorkoutLog>
    suspend fun deleteLog(id: String): Result<Unit>
    suspend fun finalizeWorkoutSession(logId: String, systemicFatigue: Int, jointPainReport: List<JointPainEntry>): Result<WorkoutLog>
    suspend fun getPreviousSessionForDay(planId: String, dayId: String): Result<WorkoutLog?>
    suspend fun findOpenLogForDay(planId: String, dayId: String, date: String): Result<WorkoutLog?>
}

