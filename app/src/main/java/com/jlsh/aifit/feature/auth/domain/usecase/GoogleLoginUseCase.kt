package com.jlsh.aifit.feature.auth.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.auth.domain.model.AuthToken
import com.jlsh.aifit.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case that authenticates the user using Google Sign-In.
 *
 * @param repository Authentication repository.
 */
class GoogleLoginUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    /**
     * Exchange the Google ID token for a session in the backend.
     *
     * @param idToken JWT token obtained from Credential Manager/Google flow.
     * @return [Result.Success] with the session token, or [Result.Error] if the backend rejects the token.
     */
    suspend operator fun invoke(idToken: String): Result<AuthToken> =
        repository.googleLogin(idToken)
}

