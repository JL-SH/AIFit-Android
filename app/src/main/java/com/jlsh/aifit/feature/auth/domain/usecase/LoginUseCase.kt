package com.jlsh.aifit.feature.auth.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.auth.domain.model.AuthToken
import com.jlsh.aifit.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthToken> =
        repository.login(email, password)
}

