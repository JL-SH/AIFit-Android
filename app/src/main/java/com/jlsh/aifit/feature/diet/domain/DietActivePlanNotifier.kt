package com.jlsh.aifit.feature.diet.domain

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Broadcasts when the user's active diet plan changes (e.g. after [setActiveDietPlan]).
 */
@Singleton
class DietActivePlanNotifier @Inject constructor() {

    private val _activePlanChanges = MutableSharedFlow<String>(extraBufferCapacity = 1)

    val activePlanChanges: SharedFlow<String> = _activePlanChanges.asSharedFlow()

    fun notifyActivePlanChanged(planId: String) {
        _activePlanChanges.tryEmit(planId)
    }
}
