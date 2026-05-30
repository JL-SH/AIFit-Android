package com.jlsh.aifit.feature.nutrition.domain

import com.jlsh.aifit.feature.nutrition.domain.model.NutritionLog
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Broadcasts when today's nutrition log changes locally (e.g. after [trackMeal]).
 * Hub and Home can update immediately without waiting for debounced resume refresh.
 */
@Singleton
class NutritionLogChangeNotifier @Inject constructor() {

    private val _changes = MutableSharedFlow<NutritionLog>(extraBufferCapacity = 2)

    val changes: SharedFlow<NutritionLog> = _changes.asSharedFlow()

    fun notifyLogChanged(log: NutritionLog) {
        _changes.tryEmit(log)
    }
}
