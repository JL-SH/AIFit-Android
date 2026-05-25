package com.jlsh.aifit.feature.user.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.user.domain.model.UpdateUserProfileRequest
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import com.jlsh.aifit.feature.user.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Use case that persists partial profile changes to the backend.
 *
 * @param repository User repository.
 */
class UpdateUserProfileUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    /**
     * Update the profile with the fields present in the request.
     *
     * @param request Fields to modify (null or absent fields are not sent).
     * @return [Result.Success] with the updated profile, or [Result.Error] if the API call fails.
     */
    suspend operator fun invoke(request: UpdateUserProfileRequest): Result<UserProfile> =
        repository.updateProfile(request)
}

