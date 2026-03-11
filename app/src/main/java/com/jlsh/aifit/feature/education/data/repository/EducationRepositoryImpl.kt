package com.jlsh.aifit.feature.education.data.repository

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

    override suspend fun getExerciseExplanation(exerciseId: String): Result<ContextualExplanation> =
        when (val r = safeApiCall { apiService.getExerciseExplanation(exerciseId) }) {
            is Result.Success -> Result.Success(r.data.toDomain())
            is Result.Error -> r
            else -> Result.Loading
        }

    override suspend fun getMealExplanation(mealId: String): Result<ContextualExplanation> =
        when (val r = safeApiCall { apiService.getMealExplanation(mealId) }) {
            is Result.Success -> Result.Success(r.data.toDomain())
            is Result.Error -> r
            else -> Result.Loading
        }

    override suspend fun getWhyThisExercise(exerciseId: String): Result<WhyThisExplanation> =
        when (val r = safeApiCall { apiService.getWhyThisExercise(exerciseId) }) {
            is Result.Success -> Result.Success(r.data.toDomain())
            is Result.Error -> r
            else -> Result.Loading
        }

    override suspend fun getWhyThisMeal(mealId: String): Result<WhyThisExplanation> =
        when (val r = safeApiCall { apiService.getWhyThisMeal(mealId) }) {
            is Result.Success -> Result.Success(r.data.toDomain())
            is Result.Error -> r
            else -> Result.Loading
        }

    override suspend fun getGlossaryTerm(term: String): Result<GlossaryDefinition> =
        when (val r = safeApiCall { apiService.getGlossaryTerm(term) }) {
            is Result.Success -> Result.Success(r.data.toDomain())
            is Result.Error -> r
            else -> Result.Loading
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
}

