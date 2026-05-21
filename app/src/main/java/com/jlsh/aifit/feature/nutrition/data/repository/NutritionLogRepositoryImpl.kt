package com.jlsh.aifit.feature.nutrition.data.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.feature.nutrition.data.api.NutritionLogApiService
import com.jlsh.aifit.feature.nutrition.data.dto.AnalyzeMealFromTextRequestDto
import com.jlsh.aifit.feature.nutrition.data.dto.TrackMealRequestDto
import com.jlsh.aifit.feature.nutrition.data.local.NutritionLogDao
import com.jlsh.aifit.feature.nutrition.data.mapper.NutritionMapper.toDomain
import com.jlsh.aifit.feature.nutrition.data.mapper.NutritionMapper.toEntity
import com.jlsh.aifit.feature.nutrition.domain.model.MealLog
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionLog
import com.jlsh.aifit.feature.nutrition.domain.repository.NutritionLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Implementación de [NutritionLogRepository] con caché diaria en Room y sincronización con la API.
 */
class NutritionLogRepositoryImpl @Inject constructor(
    private val apiService: NutritionLogApiService,
    private val dao: NutritionLogDao,
) : BaseRemoteDataSource(), NutritionLogRepository {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * Emite el registro del [date]: primero caché Room si existe, luego datos del servidor.
     *
     * @param date Fecha del registro en formato local.
     * @return Flujo de [Result] con [NutritionLog], [Result.Loading] o [Result.Error].
     */
    override fun getNutritionLog(date: LocalDate): Flow<Result<NutritionLog>> = flow {
        emit(Result.Loading)

        val epochDay = date.toEpochDay()
        val cached = dao.getByDate(epochDay)
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
    }

    /**
     * Obtiene el historial de registros entre dos fechas ISO (solo red).
     *
     * @param from Fecha inicial inclusive (ISO_LOCAL_DATE).
     * @param to Fecha final inclusive (ISO_LOCAL_DATE).
     * @return [Result.Success] con la lista de logs, o [Result.Error].
     */
    override suspend fun getNutritionHistory(from: String, to: String): Result<List<NutritionLog>> {
        return when (val remote = safeApiCall { apiService.getNutritionHistory(from, to) }) {
            is Result.Success -> Result.Success(remote.data.map { it.toDomain() })
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    /**
     * Registra una comida manual e invalida la caché del día indicado en [TrackMealRequestDto.date].
     *
     * @param request Datos de la comida y alimentos.
     * @return [Result.Success] con el [MealLog] creado, o [Result.Error].
     */
    override suspend fun trackMeal(request: TrackMealRequestDto): Result<MealLog> {
        return when (val remote = safeApiCall { apiService.trackMeal(request) }) {
            is Result.Success -> {
                invalidateCache(request.date)
                Result.Success(remote.data.toDomain())
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    /**
     * Analiza texto libre con IA, registra la comida inferida e invalida la caché del día.
     *
     * @param request Texto, tipo de comida, hora y fecha.
     * @return [Result.Success] con el [MealLog] generado, o [Result.Error].
     */
    override suspend fun analyzeMealFromText(request: AnalyzeMealFromTextRequestDto): Result<MealLog> {
        return when (val remote = safeApiCall { apiService.analyzeMealFromText(request) }) {
            is Result.Success -> {
                invalidateCache(request.date)
                Result.Success(remote.data.toDomain())
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    /**
     * Elimina una comida registrada e invalida la caché del día actual.
     *
     * @param mealId Identificador de la comida a eliminar.
     * @return [Result.Success] tras confirmación del servidor, o [Result.Error].
     */
    override suspend fun deleteMealLog(mealId: String): Result<Unit> {
        return when (val remote = safeApiCall { apiService.deleteMealLog(mealId) }) {
            is Result.Success -> {
                // Invalidate today's cache
                invalidateCache(LocalDate.now().format(dateFormatter))
                Result.Success(Unit)
            }
            is Result.Error -> remote
            else -> Result.Loading
        }
    }

    private suspend fun invalidateCache(dateStr: String) {
        runCatching {
            val date = LocalDate.parse(dateStr, dateFormatter)
            dao.deleteByDate(date.toEpochDay())
        }
    }
}

