package com.jlsh.aifit.feature.workout.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import com.jlsh.aifit.feature.workout.domain.repository.WorkoutRepository
import javax.inject.Inject

class GetPreviousSessionForDayUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    suspend operator fun invoke(planId: String, dayId: String): Result<WorkoutLog?> =
        repository.getPreviousSessionForDay(planId, dayId)
}

