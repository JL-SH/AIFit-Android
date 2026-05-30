package com.jlsh.aifit.feature.nutrition.domain.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.nutrition.data.dto.AnalyzeMealFromTextRequestDto
import com.jlsh.aifit.feature.nutrition.data.dto.TrackMealRequestDto
import com.jlsh.aifit.feature.nutrition.data.dto.UpdateMealRequestDto
import com.jlsh.aifit.feature.nutrition.domain.model.MealLog
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface NutritionLogRepository {
    fun getNutritionLog(date: LocalDate): Flow<Result<NutritionLog>>
    suspend fun getNutritionHistory(from: String, to: String): Result<List<NutritionLog>>
    suspend fun trackMeal(request: TrackMealRequestDto): Result<MealLog>
    suspend fun updateMealLog(mealId: String, request: UpdateMealRequestDto): Result<MealLog>
    suspend fun analyzeMealFromText(request: AnalyzeMealFromTextRequestDto): Result<MealLog>
    suspend fun deleteMealLog(mealId: String): Result<Unit>
}

