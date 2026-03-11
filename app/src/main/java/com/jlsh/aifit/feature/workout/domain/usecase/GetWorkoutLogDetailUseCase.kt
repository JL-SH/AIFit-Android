package com.jlsh.aifit.feature.workout.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import com.jlsh.aifit.feature.workout.domain.repository.WorkoutRepository
import javax.inject.Inject

class GetWorkoutLogDetailUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    suspend operator fun invoke(id: String): Result<WorkoutLog> =
        repository.getLogDetail(id)
}

