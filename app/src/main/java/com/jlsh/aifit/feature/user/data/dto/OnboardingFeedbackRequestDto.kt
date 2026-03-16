package com.jlsh.aifit.feature.user.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class OnboardingFeedbackRequestDto(
    val feedback: String? = null,
)

