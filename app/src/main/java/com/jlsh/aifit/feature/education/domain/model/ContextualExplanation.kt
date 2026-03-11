package com.jlsh.aifit.feature.education.domain.model

enum class ExplanationReferenceType {
    EXERCISE,
    MEAL,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): ExplanationReferenceType =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

enum class KnowledgeLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): KnowledgeLevel =
            value?.let { runCatching { valueOf(it) }.getOrDefault(UNKNOWN) } ?: UNKNOWN
    }
}

data class ContextualExplanation(
    val id: String,
    val referenceType: ExplanationReferenceType,
    val referenceId: String,
    val referenceName: String,
    val content: String,
    val knowledgeLevel: KnowledgeLevel,
    val generatedAt: String,
)

