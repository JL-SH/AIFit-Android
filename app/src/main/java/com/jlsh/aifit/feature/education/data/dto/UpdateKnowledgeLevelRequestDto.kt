package com.jlsh.aifit.feature.education.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateKnowledgeLevelRequestDto(
    val knowledgeLevel: String,
)

