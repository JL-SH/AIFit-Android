package com.jlsh.aifit.feature.workout.domain

import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog

/**
 * Payload when today's workout history changes (e.g. session finalized and locked in Room).
 */
data class WorkoutHistoryChange(
    val log: WorkoutLog,
)
