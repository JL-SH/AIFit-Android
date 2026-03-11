package com.jlsh.aifit.feature.progress.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progress.data.dto.LogBodyWeightRequestDto
import com.jlsh.aifit.feature.progress.domain.model.BodyWeightLog
import com.jlsh.aifit.feature.progress.domain.repository.BodyWeightRepository
import javax.inject.Inject

class LogBodyWeightUseCase @Inject constructor(
    private val repository: BodyWeightRepository,
) {
    suspend operator fun invoke(request: LogBodyWeightRequestDto): Result<BodyWeightLog> =
        repository.logWeight(request)
}

