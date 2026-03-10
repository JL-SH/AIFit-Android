package com.jlsh.aifit.feature.user.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import com.jlsh.aifit.feature.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    operator fun invoke(): Flow<Result<UserProfile>> = repository.getProfile()
}

