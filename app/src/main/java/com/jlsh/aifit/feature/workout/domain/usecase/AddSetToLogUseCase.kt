package com.jlsh.aifit.feature.workout.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.workout.data.dto.LogWorkoutSetRequestDto
import com.jlsh.aifit.feature.workout.domain.repository.WorkoutRepository
import javax.inject.Inject

/**
 * Caso de uso para registrar una serie adicional en un log de sesión ya creado.
 */
class AddSetToLogUseCase @Inject constructor(
    private val repository: WorkoutRepository,
) {
    /**
     * Envía una serie al backend y la asocia al log indicado.
     *
     * @param logId Identificador del log de sesión de entrenamiento.
     * @param set Datos de la serie (ejercicio, repeticiones, peso, etc.).
     * @return [Result.Success] si la serie se persiste, o [Result.Error] en caso de fallo.
     */
    suspend operator fun invoke(logId: String, set: LogWorkoutSetRequestDto): Result<Unit> =
        repository.addSetToLog(logId, set)
}
