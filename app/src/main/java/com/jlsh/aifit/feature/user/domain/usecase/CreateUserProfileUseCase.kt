package com.jlsh.aifit.feature.user.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.user.domain.model.CreateUserProfileRequest
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import com.jlsh.aifit.feature.user.domain.repository.UserRepository
import javax.inject.Inject

class CreateUserProfileUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(request: CreateUserProfileRequest): Result<UserProfile> =
        repository.createProfile(request)
}

