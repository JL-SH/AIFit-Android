package com.jlsh.aifit.feature.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseDto(
    val token: String,
    val userId: String,
    val email: String,
    val name: String,
    val expiresIn: Long,
    val profileComplete: Boolean,
)

