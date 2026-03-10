package com.jlsh.aifit.feature.auth.domain.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.auth.domain.model.AuthToken

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthToken>
    suspend fun register(email: String, password: String, name: String): Result<AuthToken>
    suspend fun googleLogin(idToken: String): Result<AuthToken>
}

