package com.jlsh.aifit.feature.workout.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.workout.data.dto.LogWorkoutSetRequestDto
import com.jlsh.aifit.feature.workout.domain.repository.WorkoutRepository
import javax.inject.Inject

/**
 * Use case to log an additional series in an already created session log.
 */
class AddSetToLogUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    /**
     * Sends a series to the backend and associates it with the indicated log.
     *
     * @param logId Identifier of the training session log.
     * @param set Set data (exercise, reps, weight, etc.).
     * @return [Result.Success] if the string is persisted, or [Result.Error] on failure.
     */
    suspend operator fun invoke(logId: String, set: LogWorkoutSetRequestDto): Result<Unit> =
        repository.addSetToLog(logId, set)
}
