package com.jlsh.aifit.feature.workout.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.workout.domain.model.JointPainEntry
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import com.jlsh.aifit.feature.workout.domain.repository.WorkoutRepository
import javax.inject.Inject

class FinalizeWorkoutSessionUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    suspend operator fun invoke(
        logId: String,
        systemicFatigue: Int,
        jointPainReport: List<JointPainEntry>,
    ): Result<WorkoutLog> =
        repository.finalizeWorkoutSession(logId, systemicFatigue, jointPainReport)
}

