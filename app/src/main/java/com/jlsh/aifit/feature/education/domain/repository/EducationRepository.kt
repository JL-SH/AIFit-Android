package com.jlsh.aifit.feature.education.domain.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.education.domain.model.ContextualExplanation
import com.jlsh.aifit.feature.education.domain.model.GlossaryDefinition
import com.jlsh.aifit.feature.education.domain.model.WhyThisExplanation

interface EducationRepository {
    suspend fun getExerciseExplanation(exerciseId: String): Result<ContextualExplanation>
    suspend fun getMealExplanation(mealId: String): Result<ContextualExplanation>
    suspend fun getWhyThisExercise(exerciseId: String): Result<WhyThisExplanation>
    suspend fun getWhyThisMeal(mealId: String): Result<WhyThisExplanation>
    suspend fun getGlossaryTerm(term: String): Result<GlossaryDefinition>
    suspend fun getHistory(): Result<List<ContextualExplanation>>
    suspend fun updateKnowledgeLevel(level: String): Result<String>
}

