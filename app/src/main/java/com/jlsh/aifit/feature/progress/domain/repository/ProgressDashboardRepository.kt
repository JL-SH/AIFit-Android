package com.jlsh.aifit.feature.progress.domain.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.progress.domain.model.ProgressDashboard
import com.jlsh.aifit.feature.progress.domain.model.WeeklyProgressSummary

interface ProgressDashboardRepository {
    suspend fun getDashboard(from: String, to: String): Result<ProgressDashboard>
    suspend fun getWeeklySummary(): Result<WeeklyProgressSummary>
}

