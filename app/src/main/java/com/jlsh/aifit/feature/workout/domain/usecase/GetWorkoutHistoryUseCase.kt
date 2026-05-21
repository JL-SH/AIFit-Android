package com.jlsh.aifit.feature.workout.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import com.jlsh.aifit.feature.workout.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para observar el historial de sesiones de entrenamiento registradas.
 */
class GetWorkoutHistoryUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    /**
     * Obtiene un flujo de logs filtrados opcionalmente por plan y rango de fechas.
     *
     * @param planId Filtra por plan de entrenamiento; null devuelve todos los planes.
     * @param from Fecha inicio inclusive en formato ISO (`yyyy-MM-dd`); null sin límite inferior.
     * @param to Fecha fin inclusive en formato ISO; null sin límite superior.
     * @return Flujo que emite caché local y luego datos de red reconciliados.
     */
    operator fun invoke(
        planId: String? = null,
        from: String? = null,
        to: String? = null,
    ): Flow<Result<List<WorkoutLog>>> = repository.getHistory(planId, from, to)
}
