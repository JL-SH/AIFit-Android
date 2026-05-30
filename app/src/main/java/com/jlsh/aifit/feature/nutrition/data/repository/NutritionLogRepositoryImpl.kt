package com.jlsh.aifit.feature.nutrition.data.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.feature.nutrition.data.api.NutritionLogApiService
import com.jlsh.aifit.feature.nutrition.data.dto.AnalyzeMealFromTextRequestDto
import com.jlsh.aifit.feature.nutrition.data.dto.TrackMealRequestDto
import com.jlsh.aifit.feature.nutrition.data.dto.UpdateMealRequestDto
import com.jlsh.aifit.feature.nutrition.data.local.NutritionLogDao
import com.jlsh.aifit.feature.nutrition.data.mapper.NutritionMapper.toDomain
import com.jlsh.aifit.feature.nutrition.data.mapper.NutritionMapper.toEntity
import com.jlsh.aifit.feature.nutrition.domain.NutritionLogChangeNotifier
import com.jlsh.aifit.feature.nutrition.domain.model.MealLog
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionLog
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Deployed [NutritionLogRepository] with daily cache in Room and sync with API.
 */
class NutritionLogRepositoryImpl @Inject constructor(
    private val apiService: NutritionLogApiService,
    private val dao: NutritionLogDao,
    private val nutritionLogChangeNotifier: NutritionLogChangeNotifier,
) : BaseRemoteDataSource(), NutritionLogRepository {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * Outputs the record on [date]: first Room cache if it exists, then server data.
     *
     * @param date Date of the record in local format.
     * @return Flow of [Result] with [NutritionLog], [Result.Loading], or [Result.Error].
     */
    override fun getNutritionLog(date: LocalDate): Flow<Result<NutritionLog>> = flow {
        emit(Result.Loading)

        val epochDay = date.toEpochDay()
        val cached = withContext(Dispatchers.IO) { dao.getByDate(epochDay) }
        if (cached != null) {
            emit(Result.Success(cached.toDomain()))
        }

        val dateStr = date.format(dateFormatter)
        when (val remote = safeApiCall { apiService.getNutritionLog(dateStr) }) {
            is Result.Success -> {
                val log = remote.data.toDomain()
                dao.upsert(log.toEntity())
                emit(Result.Success(log))
            }
            is Result.Error -> {
                if (cached == null) emit(remote)
            }
            else -> Unit
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Gets the log history between two ISO dates (network only).
     *
     * @param from Starting date inclusive (ISO_LOCAL_DATE).
     * @param to End date inclusive (ISO_LOCAL_DATE).
     * @return [Result.Success] with the list of logs, or [Result.Error].
     */
    override suspend fun getNutritionHistory(from: String, to: String): Result<List<NutritionLog>> {
        return when (val remote = safeApiCall { apiService.getNutritionHistory(from, to) }) {
            is Result.Success -> Result.Success(remote.data.map { it.toDomain() })
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    /**
     * Records a manual meal, updates local cache, and notifies listeners.
     *
     * @param request Food and meal data.
     * @return [Result.Success] with the created [MealLog], or [Result.Error].
     */
    override suspend fun trackMeal(request: TrackMealRequestDto): Result<MealLog> {
        return when (val remote = safeApiCall { apiService.trackMeal(request) }) {
            is Result.Success -> {
                val meal = remote.data.toDomain()
                val date = LocalDate.parse(request.date, dateFormatter)
                mergeMealIntoCache(date, meal)
                refreshDayLogCache(date)
                Result.Success(meal)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    override suspend fun updateMealLog(mealId: String, request: UpdateMealRequestDto): Result<MealLog> {
        return when (val remote = safeApiCall { apiService.updateMealLog(mealId, request) }) {
            is Result.Success -> {
                val meal = remote.data.toDomain()
                refreshDayLogCache(LocalDate.now())
                Result.Success(meal)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    /**
     * Analyzes free text with AI, records inferred food, and refreshes the day's cache.
     *
     * @param request Text, food type, time and date.
     * @return [Result.Success] with the generated [MealLog], or [Result.Error].
     */
    override suspend fun analyzeMealFromText(request: AnalyzeMealFromTextRequestDto): Result<MealLog> {
        return when (val remote = safeApiCall { apiService.analyzeMealFromText(request) }) {
            is Result.Success -> {
                val meal = remote.data.toDomain()
                val date = LocalDate.parse(request.date, dateFormatter)
                mergeMealIntoCache(date, meal)
                refreshDayLogCache(date)
                Result.Success(meal)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    /**
     * Delete a recorded meal and refresh the current day's cache.
     *
     * @param mealId Identifier of the meal to delete.
     * @return [Result.Success] upon server confirmation, or [Result.Error].
     */
    override suspend fun deleteMealLog(mealId: String): Result<Unit> {
        return when (val remote = safeApiCall { apiService.deleteMealLog(mealId) }) {
            is Result.Success -> {
                refreshDayLogCache(LocalDate.now())
                Result.Success(Unit)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    private suspend fun mergeMealIntoCache(date: LocalDate, meal: MealLog) {
        val epochDay = date.toEpochDay()
        val existing = dao.getByDate(epochDay)?.toDomain()
        val base = existing ?: NutritionLog(
            id = "local-$epochDay",
            date = date,
            totalCalories = 0,
            totalProteinGrams = 0.0,
            totalCarbsGrams = 0.0,
            totalFatGrams = 0.0,
            meals = emptyList(),
        )
        if (base.meals.any { it.id == meal.id }) return

        val updated = base.copy(
            meals = base.meals + meal,
            totalCalories = base.totalCalories + meal.calories,
            totalProteinGrams = base.totalProteinGrams + meal.proteinGrams,
            totalCarbsGrams = base.totalCarbsGrams + meal.carbsGrams,
            totalFatGrams = base.totalFatGrams + meal.fatGrams,
        )
        dao.upsert(updated.toEntity())
        nutritionLogChangeNotifier.notifyLogChanged(updated)
    }

    private suspend fun refreshDayLogCache(date: LocalDate): NutritionLog? {
        val dateStr = date.format(dateFormatter)
        return when (val remote = safeApiCall { apiService.getNutritionLog(dateStr) }) {
            is Result.Success -> {
                val log = remote.data.toDomain()
                dao.upsert(log.toEntity())
                nutritionLogChangeNotifier.notifyLogChanged(log)
                log
            }
            else -> dao.getByDate(date.toEpochDay())?.toDomain()
        }
    }
}
