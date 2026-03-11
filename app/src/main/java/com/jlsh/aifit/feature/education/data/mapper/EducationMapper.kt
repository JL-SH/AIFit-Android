package com.jlsh.aifit.feature.education.data.mapper

import com.jlsh.aifit.feature.education.data.dto.ContextualExplanationResponseDto
import com.jlsh.aifit.feature.education.data.dto.GlossaryDefinitionResponseDto
import com.jlsh.aifit.feature.education.data.dto.WhyThisResponseDto
import com.jlsh.aifit.feature.education.domain.model.ContextualExplanation
import com.jlsh.aifit.feature.education.domain.model.ExplanationReferenceType
import com.jlsh.aifit.feature.education.domain.model.GlossaryDefinition
import com.jlsh.aifit.feature.education.domain.model.KnowledgeLevel
import com.jlsh.aifit.feature.education.domain.model.WhyThisExplanation

object EducationMapper {

    fun ContextualExplanationResponseDto.toDomain(): ContextualExplanation = ContextualExplanation(
        id = id,
        referenceType = ExplanationReferenceType.fromString(referenceType),
        referenceId = referenceId,
        referenceName = referenceName,
        content = content,
        knowledgeLevel = KnowledgeLevel.fromString(knowledgeLevelAtGeneration),
        generatedAt = generatedAt,
    )

    fun WhyThisResponseDto.toDomain(): WhyThisExplanation = WhyThisExplanation(
        referenceType = ExplanationReferenceType.fromString(referenceType),
        referenceId = referenceId,
        referenceName = referenceName,
        explanation = explanation,
        knowledgeLevel = KnowledgeLevel.fromString(knowledgeLevelAtGeneration),
    )

    fun GlossaryDefinitionResponseDto.toDomain(): GlossaryDefinition = GlossaryDefinition(
        term = term,
        definition = definition,
        category = knowledgeLevel,
        relatedTerms = relatedTerms,
    )
}

