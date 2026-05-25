package com.jlsh.aifit.feature.nutrition.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionLog
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionLogRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use case that obtains the nutritional record for a specific day (cache and network).
 *
 * @param repository Nutritional registry repository.
 */
class GetNutritionLogUseCase @Inject constructor(
    private val repository: NutritionLogRepository,
) {
    /**
     * Issue the log of the day: first local cache if it exists, then synchronize with the server.
     *
     * @param date Date of the record to consult.
     * @return Flow of [Result] with [NutritionLog], [Result.Loading], or [Result.Error].
     */
    operator fun invoke(date: LocalDate): Flow<Result<NutritionLog>> =
        repository.getNutritionLog(date)
}
