package com.jlsh.aifit.feature.progress.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progress.domain.model.ProgressDashboard
import com.jlsh.aifit.feature.progress.domain.repository.ProgressDashboardRepository
import javax.inject.Inject

/**
 * Use case that gets the aggregated progress panel for a date range.
 *
 * @param repository Progress dashboard repository.
 */
class GetProgressDashboardUseCase @Inject constructor(
    private val repository: ProgressDashboardRepository,
) {
    /**
     * Load adherence, weight, nutrition and strength metrics in the indicated range.
     *
     * @param from Period start date in local ISO format (`yyyy-MM-dd`).
     * @param to Period end date in local ISO format (`yyyy-MM-dd`).
     * @return [Result.Success] with [ProgressDashboard], or [Result.Error] if the query fails.
     */
    suspend operator fun invoke(from: String, to: String): Result<ProgressDashboard> =
        repository.getDashboard(from, to)
}

