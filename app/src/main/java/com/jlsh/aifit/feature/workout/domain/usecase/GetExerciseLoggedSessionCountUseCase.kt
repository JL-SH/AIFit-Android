package com.jlsh.aifit.feature.workout.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progression.domain.ExerciseProgressionSessionCounter
import com.jlsh.aifit.feature.workout.domain.repository.WorkoutRepository
import javax.inject.Inject

/**
 * Returns how many workout sessions include logged sets for a plan exercise.
 */
class GetExerciseLoggedSessionCountUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    suspend operator fun invoke(exerciseId: String): Result<Int> =
        when (val result = repository.getExerciseProgression(exerciseId)) {
            is Result.Success -> Result.Success(
                ExerciseProgressionSessionCounter.countDistinctSessions(
                    result.data.entries.map { it.date },
                ),
            )
            is Result.Error -> result
            else -> Result.Loading
        }
}
