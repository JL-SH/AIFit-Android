package com.jlsh.aifit.feature.auth.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.auth.domain.model.AuthToken
import com.jlsh.aifit.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case that registers a new account and returns the initial session.
 *
 * @param repository Authentication repository.
 */
class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    /**
     * Create a user with email, password and visible name.
     *
     * @param email Email of the new user.
     * @param password Password chosen by the user.
     * @param name Name to display in the profile.
     * @return [Result.Success] with the session token after registration, or [Result.Error] if it fails.
     */
    suspend operator fun invoke(email: String, password: String, name: String): Result<AuthToken> =
        repository.register(email, password, name)
}

