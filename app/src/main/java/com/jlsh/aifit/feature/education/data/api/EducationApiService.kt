package com.jlsh.aifit.feature.education.data.api

import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.education.data.dto.ContextualExplanationResponseDto
import com.jlsh.aifit.feature.education.data.dto.GlossaryDefinitionResponseDto
import com.jlsh.aifit.feature.education.data.dto.UpdateKnowledgeLevelRequestDto
import com.jlsh.aifit.feature.education.data.dto.WhyThisResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface EducationApiService {

    @GET("education/exercises/{id}/explanation")
    suspend fun getExerciseExplanation(@Path("id") exerciseId: String): ApiResponse<ContextualExplanationResponseDto>

    @GET("education/meals/{id}/explanation")
    suspend fun getMealExplanation(@Path("id") mealId: String): ApiResponse<ContextualExplanationResponseDto>

    @GET("education/exercises/{id}/why")
    suspend fun getWhyThisExercise(@Path("id") exerciseId: String): ApiResponse<WhyThisResponseDto>

    @GET("education/meals/{id}/why")
    suspend fun getWhyThisMeal(@Path("id") mealId: String): ApiResponse<WhyThisResponseDto>

    @GET("education/glossary/{term}")
    suspend fun getGlossaryTerm(@Path("term") term: String): ApiResponse<GlossaryDefinitionResponseDto>

    @GET("education/history")
    suspend fun getHistory(): ApiResponse<List<ContextualExplanationResponseDto>>

    @PATCH("education/knowledge-level")
    suspend fun updateKnowledgeLevel(@Body request: UpdateKnowledgeLevelRequestDto): ApiResponse<String>
}

