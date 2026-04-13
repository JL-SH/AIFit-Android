package com.jlsh.aifit.feature.education.data.repository

import android.util.Log
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.feature.education.data.api.EducationApiService
import com.jlsh.aifit.feature.education.data.dto.UpdateKnowledgeLevelRequestDto
import com.jlsh.aifit.feature.education.data.mapper.EducationMapper.toDomain
import com.jlsh.aifit.feature.education.domain.model.ContextualExplanation
import com.jlsh.aifit.feature.education.domain.model.GlossaryDefinition
import com.jlsh.aifit.feature.education.domain.model.WhyThisExplanation
import com.jlsh.aifit.feature.education.domain.repository.EducationRepository
import javax.inject.Inject

class EducationRepositoryImpl @Inject constructor(
    private val apiService: EducationApiService,
) : BaseRemoteDataSource(), EducationRepository {

    override suspend fun getExerciseExplanation(exerciseId: String): Result<ContextualExplanation> {
        Log.d("AIFIT_DEBUG", "repo: getExerciseExplanation llamando API id=$exerciseId")
        return when (val r = safeApiCall { apiService.getExerciseExplanation(exerciseId) }) {
            is Result.Success -> {
                Log.d("AIFIT_DEBUG", "repo: getExerciseExplanation SUCCESS dto=${r.data}")
                Result.Success(r.data.toDomain())
            }
            is Result.Error -> {
                logDetailedError("getExerciseExplanation", r.exception)
                r
            }
            else -> Result.Loading
        }
    }

    override suspend fun getMealExplanation(mealId: String): Result<ContextualExplanation> {
        Log.d("AIFIT_DEBUG", "repo: getMealExplanation llamando API id=$mealId")
        return when (val r = safeApiCall { apiService.getMealExplanation(mealId) }) {
            is Result.Success -> {
                Log.d("AIFIT_DEBUG", "repo: getMealExplanation SUCCESS dto=${r.data}")
                Result.Success(r.data.toDomain())
            }
            is Result.Error -> {
                logDetailedError("getMealExplanation", r.exception)
                r
            }
            else -> Result.Loading
        }
    }

    override suspend fun getWhyThisExercise(exerciseId: String): Result<WhyThisExplanation> {
        Log.d("AIFIT_DEBUG", "repo: getWhyThisExercise llamando API id=$exerciseId")
        return when (val r = safeApiCall { apiService.getWhyThisExercise(exerciseId) }) {
            is Result.Success -> {
                Log.d("AIFIT_DEBUG", "repo: getWhyThisExercise SUCCESS dto=${r.data}")
                Result.Success(r.data.toDomain())
            }
            is Result.Error -> {
                logDetailedError("getWhyThisExercise", r.exception)
                r
            }
            else -> Result.Loading
        }
    }

    override suspend fun getWhyThisMeal(mealId: String): Result<WhyThisExplanation> {
        Log.d("AIFIT_DEBUG", "repo: getWhyThisMeal llamando API id=$mealId")
        return when (val r = safeApiCall { apiService.getWhyThisMeal(mealId) }) {
            is Result.Success -> {
                Log.d("AIFIT_DEBUG", "repo: getWhyThisMeal SUCCESS dto=${r.data}")
                Result.Success(r.data.toDomain())
            }
            is Result.Error -> {
                logDetailedError("getWhyThisMeal", r.exception)
                r
            }
            else -> Result.Loading
        }
    }

    override suspend fun getGlossaryTerm(term: String): Result<GlossaryDefinition> {
        Log.d("AIFIT_DEBUG", "repo: getGlossaryTerm llamando API term=$term")
        return when (val r = safeApiCall { apiService.getGlossaryTerm(term) }) {
            is Result.Success -> {
                Log.d("AIFIT_DEBUG", "repo: getGlossaryTerm SUCCESS dto=${r.data}")
                Result.Success(r.data.toDomain())
            }
            is Result.Error -> {
                logDetailedError("getGlossaryTerm", r.exception)
                r
            }
            else -> Result.Loading
        }
    }

    override suspend fun getHistory(): Result<List<ContextualExplanation>> =
        when (val r = safeApiCall { apiService.getHistory() }) {
            is Result.Success -> Result.Success(r.data.map { it.toDomain() })
            is Result.Error -> r
            else -> Result.Loading
        }

    override suspend fun updateKnowledgeLevel(level: String): Result<String> =
        when (val r = safeApiCall { apiService.updateKnowledgeLevel(UpdateKnowledgeLevelRequestDto(level)) }) {
            is Result.Success -> Result.Success(r.data)
            is Result.Error -> r
            else -> Result.Loading
        }

    private fun logDetailedError(method: String, exception: AppException) {
        val hint = when (exception) {
            is AppException.ServerException -> "Backend 500 — probable fallo en Gemini AI o prompt builder"
            is AppException.NetworkException -> "Sin conexión de red"
            is AppException.NotFoundException -> "Recurso no encontrado (404) — ¿el ejercicio/meal/term existe en el backend?"
            is AppException.UnknownException -> "Error desconocido: ${exception.message}"
            else -> exception::class.simpleName
        }
        Log.e(
            "AIFIT_DEBUG",
            "repo: $method ERROR type=${exception::class.simpleName} hint=$hint msg=${exception.message}",
            exception,
        )
    }
}

