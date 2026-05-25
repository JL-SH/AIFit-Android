package com.jlsh.aifit.feature.nutrition.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.nutrition.data.dto.TrackMealRequestDto
import com.jlsh.aifit.feature.nutrition.domain.model.MealLog
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionLogRepository
import javax.inject.Inject

/**
 * Use case that records a manual meal in the user's nutritional diary.
 *
 * @param repository Nutritional registry repository.
 */
class TrackMealUseCase @Inject constructor(
    private val repository: NutritionLogRepository,
) {
    /**
     * Sends the food to the backend and updates the local cache for the given day.
     *
     * @param request Food data (date, type, time, food, macros).
     * @return [Result.Success] with the [MealLog] persisted, or [Result.Error] if logging fails.
     */
    suspend operator fun invoke(request: TrackMealRequestDto): Result<MealLog> =
        repository.trackMeal(request)
}
