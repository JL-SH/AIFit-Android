package com.jlsh.aifit.feature.progress.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progress.data.dto.LogBodyWeightRequestDto
import com.jlsh.aifit.feature.progress.domain.model.BodyWeightLog
import com.jlsh.aifit.feature.progress.domain.repository.BodyWeightRepository
import javax.inject.Inject

/**
 * Caso de uso que registra un nuevo peso corporal del usuario.
 *
 * @param repository Repositorio de historial y registro de peso.
 */
class LogBodyWeightUseCase @Inject constructor(
    private val repository: BodyWeightRepository,
) {
    /**
     * Persiste el peso indicado en el backend y en la caché local.
     *
     * @param request Peso, fecha y notas opcionales del registro.
     * @return [Result.Success] con el [BodyWeightLog] creado, o [Result.Error] si falla el envío.
     */
    suspend operator fun invoke(request: LogBodyWeightRequestDto): Result<BodyWeightLog> =
        repository.logWeight(request)
}

