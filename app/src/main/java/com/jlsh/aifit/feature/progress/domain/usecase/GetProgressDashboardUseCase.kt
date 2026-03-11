package com.jlsh.aifit.feature.progress.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progress.domain.model.ProgressDashboard
import com.jlsh.aifit.feature.progress.domain.repository.ProgressDashboardRepository
import javax.inject.Inject

class GetProgressDashboardUseCase @Inject constructor(
    private val repository: ProgressDashboardRepository,
) {
    suspend operator fun invoke(from: String, to: String): Result<ProgressDashboard> =
        repository.getDashboard(from, to)
}

