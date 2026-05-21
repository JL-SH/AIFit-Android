package com.jlsh.aifit.feature.nutrition.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionTargetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso que obtiene los objetivos nutricionales vigentes del usuario.
 *
 * @param repository Repositorio de objetivos nutricionales.
 */
class GetCurrentNutritionTargetUseCase @Inject constructor(
    private val repository: NutritionTargetRepository,
) {
    /**
     * Emite el objetivo actual (calorías y macros) desde caché y/o red.
     *
     * @return Flujo de [Result] con [NutritionTarget], [Result.Loading] o [Result.Error].
     */
    operator fun invoke(): Flow<Result<NutritionTarget>> =
        repository.getCurrentTarget()
}
