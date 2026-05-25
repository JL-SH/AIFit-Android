package com.jlsh.aifit.feature.user.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import com.jlsh.aifit.feature.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case that looks at the profile of the authenticated user.
 *
 * The flow can emit local cache first and then the network result.
 *
 * @param repository User repository.
 */
class GetUserProfileUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    /**
     * Gets the profile as a reactive flow.
     *
     * @return [Flow] from [Result] with [UserProfile]; includes [Result.Loading] at the start.
     */
    operator fun invoke(): Flow<Result<UserProfile>> = repository.getProfile()
}

