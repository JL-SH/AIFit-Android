package com.jlsh.aifit.feature.workout.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.workout.domain.model.JointPainEntry
import com.jlsh.aifit.feature.workout.domain.model.WorkoutLog
import com.jlsh.aifit.feature.workout.domain.repository.WorkoutRepository
import javax.inject.Inject

/**
 * Caso de uso para cerrar una sesión de entrenamiento con fatiga y reporte articular.
 */
class FinalizeWorkoutSessionUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    /**
     * Finaliza el log bloqueando ediciones futuras y guardando métricas subjetivas.
     *
     * @param logId Identificador del log de sesión.
     * @param systemicFatigue Fatiga sistémica reportada (escala acordada con el backend).
     * @param jointPainReport Lista de molestias articulares por articulación.
     * @return [Result.Success] con el [WorkoutLog] finalizado (`isLocked`), o [Result.Error].
     */
    suspend operator fun invoke(
        logId: String,
        systemicFatigue: Int,
        jointPainReport: List<JointPainEntry>,
    ): Result<WorkoutLog> =
        repository.finalizeWorkoutSession(logId, systemicFatigue, jointPainReport)
}
