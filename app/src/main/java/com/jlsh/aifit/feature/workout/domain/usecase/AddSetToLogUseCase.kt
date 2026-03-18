package com.jlsh.aifit.feature.workout.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.workout.data.dto.LogWorkoutSetRequestDto
import com.jlsh.aifit.feature.workout.domain.repository.WorkoutRepository
import javax.inject.Inject

class AddSetToLogUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    suspend operator fun invoke(logId: String, set: LogWorkoutSetRequestDto): Result<Unit> =
        repository.addSetToLog(logId, set)
}

