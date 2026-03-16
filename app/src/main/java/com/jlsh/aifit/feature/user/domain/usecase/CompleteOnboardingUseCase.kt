package com.jlsh.aifit.feature.user.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.user.domain.model.OnboardingResult
import com.jlsh.aifit.feature.user.domain.repository.UserRepository
import javax.inject.Inject

class CompleteOnboardingUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(feedback: String? = null): Result<OnboardingResult> =
        repository.completeOnboarding(feedback)
}

