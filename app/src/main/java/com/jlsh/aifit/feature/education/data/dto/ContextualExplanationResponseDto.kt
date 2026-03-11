package com.jlsh.aifit.feature.education.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ContextualExplanationResponseDto(
    val id: String,
    val referenceType: String,
    val referenceId: String,
    val referenceName: String,
    val content: String,
    val knowledgeLevelAtGeneration: String,
    val generatedAt: String,
)

