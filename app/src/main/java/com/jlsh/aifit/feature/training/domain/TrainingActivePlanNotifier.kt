package com.jlsh.aifit.feature.training.domain

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Broadcasts when the user's active training plan changes.
 * Supports optimistic updates (before API) and confirmed updates (after Room write).
 */
@Singleton
class TrainingActivePlanNotifier @Inject constructor() {

    private val _activePlanChanges = MutableSharedFlow<ActiveTrainingPlanChange>(extraBufferCapacity = 2)

    val activePlanChanges: SharedFlow<ActiveTrainingPlanChange> = _activePlanChanges.asSharedFlow()

    /** Confirmed change after [activatePlan] persisted to Room. */
    fun notifyActivePlanChanged(planId: String) {
        _activePlanChanges.tryEmit(
            ActiveTrainingPlanChange(planId = planId, isOptimistic = false, isRevert = false),
        )
    }

    /** Immediate UI sync before the activate API returns. */
    fun notifyOptimisticActivePlanChange(planId: String, planName: String?) {
        _activePlanChanges.tryEmit(
            ActiveTrainingPlanChange(
                planId = planId,
                planName = planName,
                isOptimistic = true,
                isRevert = false,
            ),
        )
    }

    /** Activation failed; listeners should restore state from Room. */
    fun notifyActivePlanChangeReverted() {
        _activePlanChanges.tryEmit(
            ActiveTrainingPlanChange(
                planId = "",
                isOptimistic = false,
                isRevert = true,
            ),
        )
    }
}
