package com.jlsh.aifit.feature.auth.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.auth.domain.model.AuthToken
import com.jlsh.aifit.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case that authenticates the user with email and password.
 *
 * @param repository Authentication repository.
 */
class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    /**
     * Sign in with the indicated credentials.
     *
     * @param email Email of the user.
     * @param password Password in plain text.
     * @return [Result.Success] with the session token, or [Result.Error] if authentication fails.
     */
    suspend operator fun invoke(email: String, password: String): Result<AuthToken> =
        repository.login(email, password)
}

