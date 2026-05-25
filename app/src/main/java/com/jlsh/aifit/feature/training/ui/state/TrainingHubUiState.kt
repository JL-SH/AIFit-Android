package com.jlsh.aifit.feature.training.ui.state

import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.training.domain.model.TrainingDay
import com.jlsh.aifit.feature.training.domain.model.TrainingPlan

/**
 * Training hub UI status: active plan, list and filters.
 */
sealed interface TrainingHubUiState {

    /** Initial load or refresh without previous data on the screen.*/
    data object Loading : TrainingHubUiState

    /**
     * Error obtaining plans.
     *
     * @property message Error message to display to the user.
     */
    data class Error(val message: String) : TrainingHubUiState

    /** The user has saved plans but none are active.*/
    data object NoActivePlan : TrainingHubUiState

    /**
     * There is an active plan with weekly progress and listing of all plans.
     *
     * @property plan Currently active plan.
     * @property currentWeek Current week within the plan duration (1-based).
     * @property nextDay Next suggested training day, or null if there are no days loaded.
     * @property allPlans All user plans for the list below.
     * @property selectedFilter State filter applied to the listing; null shows all.
     */
    data class ActivePlan(
        val plan: TrainingPlan,
        val currentWeek: Int,
        val nextDay: TrainingDay?,
        val allPlans: List<TrainingPlan>,
        val selectedFilter: PlanStatus?,
    ) : TrainingHubUiState
}
