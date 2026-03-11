package com.jlsh.aifit.feature.workout.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import com.jlsh.aifit.feature.workout.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWorkoutHistoryUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    operator fun invoke(
        planId: String? = null,
        from: String? = null,
        to: String? = null,
    ): Flow<Result<List<WorkoutLog>>> = repository.getHistory(planId, from, to)
}

