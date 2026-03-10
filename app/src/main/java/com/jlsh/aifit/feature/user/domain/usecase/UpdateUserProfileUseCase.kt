package com.jlsh.aifit.feature.user.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.user.domain.model.UpdateUserProfileRequest
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import com.jlsh.aifit.feature.user.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(request: UpdateUserProfileRequest): Result<UserProfile> =
        repository.updateProfile(request)
}

