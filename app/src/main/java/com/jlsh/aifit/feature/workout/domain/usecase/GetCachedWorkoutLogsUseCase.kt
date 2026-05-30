package com.jlsh.aifit.feature.workout.domain.usecase

import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import com.jlsh.aifit.feature.workout.domain.repository.WorkoutRepository
import javax.inject.Inject

/** Reads workout logs from Room only (no network). */
class GetCachedWorkoutLogsUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    suspend operator fun invoke(
        planId: String? = null,
        from: String? = null,
        to: String? = null,
    ): List<WorkoutLog> = repository.getCachedWorkoutLogs(planId = planId, from = from, to = to)
}
