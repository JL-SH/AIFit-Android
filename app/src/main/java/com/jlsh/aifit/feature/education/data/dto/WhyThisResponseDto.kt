package com.jlsh.aifit.feature.education.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class WhyThisResponseDto(
    val referenceType: String,
    val referenceId: String,
    val referenceName: String,
    val explanation: String,
    val knowledgeLevelAtGeneration: String,
)

