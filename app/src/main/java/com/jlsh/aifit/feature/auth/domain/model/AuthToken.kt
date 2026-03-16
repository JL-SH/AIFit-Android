package com.jlsh.aifit.feature.auth.domain.model

data class AuthToken(
    val token: String,
    val userId: String,
    val email: String,
    val name: String,
    val expiresIn: Long,
    val profileComplete: Boolean,
)

