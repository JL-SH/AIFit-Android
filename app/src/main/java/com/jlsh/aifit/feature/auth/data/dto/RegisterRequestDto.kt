package com.jlsh.aifit.feature.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val name: String,
)

