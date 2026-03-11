package com.jlsh.aifit.feature.progress.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progress.domain.model.BodyWeightLog
import com.jlsh.aifit.feature.progress.domain.repository.BodyWeightRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBodyWeightHistoryUseCase @Inject constructor(
    private val repository: BodyWeightRepository,
) {
    operator fun invoke(from: String, to: String): Flow<Result<List<BodyWeightLog>>> =
        repository.getHistory(from, to)
}

