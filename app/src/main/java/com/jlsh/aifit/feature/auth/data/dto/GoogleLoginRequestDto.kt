package com.jlsh.aifit.feature.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GoogleLoginRequestDto(
    val idToken: String,
)

