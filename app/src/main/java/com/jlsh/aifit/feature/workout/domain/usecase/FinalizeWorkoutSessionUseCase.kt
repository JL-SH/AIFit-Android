package com.jlsh.aifit.feature.workout.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.workout.domain.model.JointPainEntry
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import com.jlsh.aifit.feature.workout.domain.repository.WorkoutRepository
import javax.inject.Inject

/**
 * Use case to close a training session with fatigue and joint report.
 */
class FinalizeWorkoutSessionUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    /**
     * Finalize the log by blocking future edits and saving subjective metrics.
     *
     * @param logId Identifier of the session log.
     * @param systemicFatigue Reported systemic fatigue (scale agreed upon with backend).
     * @param jointPainReport List of joint pains per joint.
     * @return [Result.Success] with the finalized [WorkoutLog] (`isLocked`), or [Result.Error].
     */
    suspend operator fun invoke(
        logId: String,
        systemicFatigue: Int,
        jointPainReport: List<JointPainEntry>,
    ): Result<WorkoutLog> =
        repository.finalizeWorkoutSession(logId, systemicFatigue, jointPainReport)
}
