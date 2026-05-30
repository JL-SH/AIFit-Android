package com.jlsh.aifit.feature.workout.domain

import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Broadcasts when workout logs change locally (e.g. after [finalizeWorkoutSession]).
 * Home can mark today's session complete without waiting for bootstrap/history network.
 */
@Singleton
class WorkoutHistoryNotifier @Inject constructor() {

    private val _changes = MutableSharedFlow<WorkoutHistoryChange>(extraBufferCapacity = 2)

    val changes: SharedFlow<WorkoutHistoryChange> = _changes.asSharedFlow()

    fun notifyWorkoutFinalized(log: WorkoutLog) {
        _changes.tryEmit(WorkoutHistoryChange(log = log))
    }
}
