package com.jlsh.aifit.feature.progress.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progress.domain.model.WeeklyProgressSummary
import com.jlsh.aifit.feature.progress.domain.repository.ProgressDashboardRepository
import javax.inject.Inject

class GetWeeklyProgressSummaryUseCase @Inject constructor(
    private val repository: ProgressDashboardRepository,
) {
    suspend operator fun invoke(): Result<WeeklyProgressSummary> =
        repository.getWeeklySummary()
}

