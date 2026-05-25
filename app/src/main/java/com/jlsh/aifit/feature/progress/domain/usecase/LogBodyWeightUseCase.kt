package com.jlsh.aifit.feature.progress.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progress.data.dto.LogBodyWeightRequestDto
import com.jlsh.aifit.feature.progress.domain.model.BodyWeightLog
import com.jlsh.aifit.feature.progress.domain.repository.BodyWeightRepository
import javax.inject.Inject

/**
 * Use case that records a new user body weight.
 *
 * @param repository History and weight log repository.
 */
class LogBodyWeightUseCase @Inject constructor(
    private val repository: BodyWeightRepository,
) {
    /**
     * The indicated weight persists in the backend and in the local cache.
     *
     * @param request Weight, date, and optional notes of the record.
     * @return [Result.Success] with the created [BodyWeightLog], or [Result.Error] if the send fails.
     */
    suspend operator fun invoke(request: LogBodyWeightRequestDto): Result<BodyWeightLog> =
        repository.logWeight(request)
}

