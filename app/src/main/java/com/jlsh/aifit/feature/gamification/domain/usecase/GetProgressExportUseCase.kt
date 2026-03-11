package com.jlsh.aifit.feature.gamification.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.gamification.domain.model.ProgressExport
import com.jlsh.aifit.feature.gamification.domain.repository.GamificationRepository
import javax.inject.Inject

class GetProgressExportUseCase @Inject constructor(
    private val repository: GamificationRepository,
) {
    suspend operator fun invoke(period: String): Result<ProgressExport> =
        repository.getExport(period)
}

