package com.jlsh.aifit.feature.education.data.mapper

import com.jlsh.aifit.feature.education.data.mapper.EducationMapper.toDomain
import com.jlsh.aifit.feature.education.domain.model.ExplanationReferenceType
import com.jlsh.aifit.feature.education.domain.model.KnowledgeLevel
import com.jlsh.aifit.testutil.*
import org.junit.Assert.*
import org.junit.Test

class EducationMapperTest {

    // ─── ContextualExplanationResponseDto.toDomain() ───────────────────────────

    @Test
    fun `ContextualExplanationResponseDto toDomain convierte correctamente todos los campos`() {
        val dto = fakeContextualExplanationResponseDto()
        val result = dto.toDomain()

        assertEquals("expl-1", result.id)
        assertEquals(ExplanationReferenceType.TRAINING_EXERCISE, result.referenceType)
        assertEquals("exercise-1", result.referenceId)
        assertEquals("Bench Press", result.referenceName)
        assertEquals("The bench press is a compound exercise that targets the chest.", result.content)
        assertEquals(KnowledgeLevel.BEGINNER, result.knowledgeLevel)
        assertEquals("2026-04-01T10:00:00", result.generatedAt)
    }

    @Test
    fun `ContextualExplanationResponseDto toDomain con referenceType desconocido mapea a UNKNOWN`() {
        val dto = fakeContextualExplanationResponseDto(referenceType = "WEIRD_TYPE")
        val result = dto.toDomain()

        assertEquals(ExplanationReferenceType.UNKNOWN, result.referenceType)
    }

    @Test
    fun `ContextualExplanationResponseDto toDomain con knowledgeLevel desconocido mapea a UNKNOWN`() {
        val dto = fakeContextualExplanationResponseDto(knowledgeLevelAtGeneration = "EXPERT")
        val result = dto.toDomain()

        assertEquals(KnowledgeLevel.UNKNOWN, result.knowledgeLevel)
    }

    // ─── WhyThisResponseDto.toDomain() ─────────────────────────────────────────

    @Test
    fun `WhyThisResponseDto toDomain convierte correctamente todos los campos`() {
        val dto = fakeWhyThisResponseDto()
        val result = dto.toDomain()

        assertEquals(ExplanationReferenceType.TRAINING_EXERCISE, result.referenceType)
        assertEquals("exercise-1", result.referenceId)
        assertEquals("Bench Press", result.referenceName)
        assertEquals("This exercise is in your plan because it targets chest development.", result.explanation)
        assertEquals(KnowledgeLevel.BEGINNER, result.knowledgeLevel)
    }

    @Test
    fun `WhyThisResponseDto toDomain con referenceType desconocido mapea a UNKNOWN`() {
        val dto = fakeWhyThisResponseDto(referenceType = "NUTRITION_SUPPLEMENT")
        val result = dto.toDomain()

        assertEquals(ExplanationReferenceType.UNKNOWN, result.referenceType)
    }

    // ─── GlossaryDefinitionResponseDto.toDomain() ──────────────────────────────

    @Test
    fun `GlossaryDefinitionResponseDto toDomain convierte correctamente todos los campos`() {
        val dto = fakeGlossaryDefinitionResponseDto()
        val result = dto.toDomain()

        assertEquals("Hypertrophy", result.term)
        assertEquals("The enlargement of an organ or tissue from the increase in size of its cells.", result.definition)
        assertEquals("BEGINNER", result.category)
        assertEquals(listOf("Progressive Overload", "Volume"), result.relatedTerms)
    }

    @Test
    fun `GlossaryDefinitionResponseDto toDomain con lista vacía de relatedTerms`() {
        val dto = fakeGlossaryDefinitionResponseDto(relatedTerms = emptyList())
        val result = dto.toDomain()

        assertTrue(result.relatedTerms.isEmpty())
    }

    // ─── Enum fallback tests ───────────────────────────────────────────────────

    @Test
    fun `ExplanationReferenceType fromString con null mapea a UNKNOWN`() {
        assertEquals(ExplanationReferenceType.UNKNOWN, ExplanationReferenceType.fromString(null))
    }

    @Test
    fun `KnowledgeLevel fromString con null mapea a UNKNOWN`() {
        assertEquals(KnowledgeLevel.UNKNOWN, KnowledgeLevel.fromString(null))
    }

    @Test
    fun `ExplanationReferenceType fromString con valor válido mapea correctamente`() {
        assertEquals(ExplanationReferenceType.MEAL, ExplanationReferenceType.fromString("MEAL"))
        assertEquals(ExplanationReferenceType.DIET_DAY, ExplanationReferenceType.fromString("DIET_DAY"))
        assertEquals(ExplanationReferenceType.TRAINING_DAY, ExplanationReferenceType.fromString("TRAINING_DAY"))
    }

    @Test
    fun `KnowledgeLevel fromString con valor válido mapea correctamente`() {
        assertEquals(KnowledgeLevel.INTERMEDIATE, KnowledgeLevel.fromString("INTERMEDIATE"))
        assertEquals(KnowledgeLevel.ADVANCED, KnowledgeLevel.fromString("ADVANCED"))
    }
}

