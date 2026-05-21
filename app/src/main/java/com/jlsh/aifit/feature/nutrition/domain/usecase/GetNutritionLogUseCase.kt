package com.jlsh.aifit.feature.nutrition.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionLog
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionLogRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso que obtiene el registro nutricional de un día concreto (caché y red).
 *
 * @param repository Repositorio del registro nutricional.
 */
class GetNutritionLogUseCase @Inject constructor(
    private val repository: NutritionLogRepository,
) {
    /**
     * Emite el log del día: primero caché local si existe, luego sincroniza con el servidor.
     *
     * @param date Fecha del registro a consultar.
     * @return Flujo de [Result] con [NutritionLog], [Result.Loading] o [Result.Error].
     */
    operator fun invoke(date: LocalDate): Flow<Result<NutritionLog>> =
        repository.getNutritionLog(date)
}
