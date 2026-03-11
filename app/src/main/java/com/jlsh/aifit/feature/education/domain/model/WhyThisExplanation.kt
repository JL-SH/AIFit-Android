package com.jlsh.aifit.feature.education.domain.model

data class WhyThisExplanation(
    val referenceType: ExplanationReferenceType,
    val referenceId: String,
    val referenceName: String,
    val explanation: String,
    val knowledgeLevel: KnowledgeLevel,
)

