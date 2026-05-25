package com.jlsh.aifit.feature.progress.ui.state

import com.jlsh.aifit.core.ui.components.layout.UiStateHost
import com.jlsh.aifit.feature.progress.domain.model.BodyWeightLog
import com.jlsh.aifit.feature.progress.domain.model.ProgressDashboard
import com.jlsh.aifit.feature.progress.domain.model.WeeklyProgressSummary
import java.time.LocalDate

/**
 * Status of the main screen of the progress dashboard.
 */
sealed class DashboardUiState {
    /** Initial charge or reload of the panel.*/
    data object Loading : DashboardUiState(), UiStateHost.Loading

    /**
     * Error obtaining the dashboard.
     *
     * @property message Text to display to the user.
     */
    data class Error(override val message: String) : DashboardUiState(), UiStateHost.Error

    /**
     * Dashboard loaded successfully.
     *
     * @property dashboard Aggregated metrics for the selected period.
     * @property selectedPeriod Label of the active temporal filter (e.g. “30 days”).
     */
    data class Success(
        val dashboard: ProgressDashboard,
        val selectedPeriod: String = "30 days",
    ) : DashboardUiState(), UiStateHost.Success
}

/**
 * Body weight history and record screen status.
 *
 * @property weightHistory Weight entries in the queried range.
 * @property isLoading Indicates whether the history is being loaded.
 * @property formWeight Text of the weight field on the form.
 * @property formDate Date selected for the new record.
 * @property formNotes Optional registry notes.
 * @property isSaving Indicates whether a weight send is in progress.
 */
data class BodyWeightUiState(
    val weightHistory: List<BodyWeightLog> = emptyList(),
    val isLoading: Boolean = true,
    val formWeight: String = "",
    val formDate: LocalDate = LocalDate.now(),
    val formNotes: String = "",
    val isSaving: Boolean = false,
)

/**
 * Weekly progress summary screen status.
 */
sealed class WeeklySummaryUiState {
    /** Uploading the weekly summary.*/
    data object Loading : WeeklySummaryUiState(), UiStateHost.Loading

    /**
     * Error getting summary.
     *
     * @property message Text to display to the user.
     */
    data class Error(override val message: String) : WeeklySummaryUiState(), UiStateHost.Error

    /**
     * Weekly summary available.
     *
     * @property summary Aggregated data for the week.
     */
    data class Success(
        val summary: WeeklyProgressSummary,
    ) : WeeklySummaryUiState(), UiStateHost.Success
}

