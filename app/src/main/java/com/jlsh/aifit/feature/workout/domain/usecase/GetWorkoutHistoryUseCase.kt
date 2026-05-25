package com.jlsh.aifit.feature.workout.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import com.jlsh.aifit.feature.workout.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to observe the history of recorded training sessions.
 */
class GetWorkoutHistoryUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    /**
     * Gets a stream of logs optionally filtered by plan and date range.
     *
     * @param planId Filter by training plan; null returns all plans.
     * @param from Start date inclusive in ISO format (`yyyy-MM-dd`); null with no lower limit.
     * @param to End date inclusive in ISO format; null with no upper limit.
     * @return Flow that emits local cache and then reconciled network data.
     */
    operator fun invoke(
        planId: String? = null,
        from: String? = null,
        to: String? = null,
    ): Flow<Result<List<WorkoutLog>>> = repository.getHistory(planId, from, to)
}
