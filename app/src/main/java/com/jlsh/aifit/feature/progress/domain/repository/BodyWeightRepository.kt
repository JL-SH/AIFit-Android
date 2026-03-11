package com.jlsh.aifit.feature.progress.domain.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progress.data.dto.LogBodyWeightRequestDto
import com.jlsh.aifit.feature.progress.domain.model.BodyWeightLog
import kotlinx.coroutines.flow.Flow

interface BodyWeightRepository {
    fun getHistory(from: String, to: String): Flow<Result<List<BodyWeightLog>>>
    suspend fun logWeight(request: LogBodyWeightRequestDto): Result<BodyWeightLog>
}

