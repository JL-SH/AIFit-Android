package com.jlsh.aifit.feature.education.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GlossaryDefinitionResponseDto(
    val term: String,
    val definition: String,
    val knowledgeLevel: String,
    val relatedTerms: List<String> = emptyList(),
)

