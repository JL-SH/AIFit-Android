package com.jlsh.aifit.feature.progress.ui.state

import com.jlsh.aifit.core.ui.components.layout.UiStateHost
import com.jlsh.aifit.feature.progress.domain.model.BodyWeightLog
import com.jlsh.aifit.feature.progress.domain.model.ProgressDashboard
import com.jlsh.aifit.feature.progress.domain.model.WeeklyProgressSummary
import java.time.LocalDate

sealed class DashboardUiState {
    data object Loading : DashboardUiState(), UiStateHost.Loading
    data class Error(override val message: String) : DashboardUiState(), UiStateHost.Error
    data class Success(
        val dashboard: ProgressDashboard,
        val selectedPeriod: String = "30 days",
    ) : DashboardUiState(), UiStateHost.Success
}

data class BodyWeightUiState(
    val weightHistory: List<BodyWeightLog> = emptyList(),
    val isLoading: Boolean = true,
    val formWeight: String = "",
    val formDate: LocalDate = LocalDate.now(),
    val formNotes: String = "",
    val isSaving: Boolean = false,
)

sealed class WeeklySummaryUiState {
    data object Loading : WeeklySummaryUiState(), UiStateHost.Loading
    data class Error(override val message: String) : WeeklySummaryUiState(), UiStateHost.Error
    data class Success(
        val summary: WeeklyProgressSummary,
    ) : WeeklySummaryUiState(), UiStateHost.Success
}

