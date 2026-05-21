package com.jlsh.aifit.feature.nutrition.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.nutrition.data.dto.TrackMealRequestDto
import com.jlsh.aifit.feature.nutrition.domain.model.MealLog
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionLogRepository
import javax.inject.Inject

/**
 * Caso de uso que registra una comida manual en el diario nutricional del usuario.
 *
 * @param repository Repositorio del registro nutricional.
 */
class TrackMealUseCase @Inject constructor(
    private val repository: NutritionLogRepository,
) {
    /**
     * Envía la comida al backend y actualiza la caché local del día indicado.
     *
     * @param request Datos de la comida (fecha, tipo, hora, alimentos, macros).
     * @return [Result.Success] con el [MealLog] persistido, o [Result.Error] si falla el registro.
     */
    suspend operator fun invoke(request: TrackMealRequestDto): Result<MealLog> =
        repository.trackMeal(request)
}
