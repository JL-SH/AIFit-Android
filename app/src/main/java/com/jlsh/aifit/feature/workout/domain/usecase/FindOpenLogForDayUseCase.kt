package com.jlsh.aifit.feature.workout.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import com.jlsh.aifit.feature.workout.domain.repository.WorkoutRepository
import javax.inject.Inject

/**
 * Finds an existing workout log for a plan day on a given date (direct API lookup).
 */
class FindOpenLogForDayUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    suspend operator fun invoke(
        planId: String,
        dayId: String,
        date: String,
    ): Result<WorkoutLog?> = repository.findOpenLogForDay(planId, dayId, date)
}
