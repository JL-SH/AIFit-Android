package com.jlsh.aifit.feature.progress.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progress.domain.model.ProgressDashboard
import com.jlsh.aifit.feature.progress.domain.repository.ProgressDashboardRepository
import javax.inject.Inject

/**
 * Caso de uso que obtiene el panel de progreso agregado para un intervalo de fechas.
 *
 * @param repository Repositorio del dashboard de progreso.
 */
class GetProgressDashboardUseCase @Inject constructor(
    private val repository: ProgressDashboardRepository,
) {
    /**
     * Carga métricas de adherencia, peso, nutrición y fuerza en el rango indicado.
     *
     * @param from Fecha de inicio del periodo en formato ISO local (`yyyy-MM-dd`).
     * @param to Fecha de fin del periodo en formato ISO local (`yyyy-MM-dd`).
     * @return [Result.Success] con [ProgressDashboard], o [Result.Error] si falla la consulta.
     */
    suspend operator fun invoke(from: String, to: String): Result<ProgressDashboard> =
        repository.getDashboard(from, to)
}

