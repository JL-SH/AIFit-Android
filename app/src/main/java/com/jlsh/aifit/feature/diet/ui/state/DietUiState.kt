package com.jlsh.aifit.feature.diet.ui.state

import com.jlsh.aifit.core.ui.components.layout.UiStateHost
import com.jlsh.aifit.feature.diet.domain.model.DietPlan

/**
 * UI status for a diet plan detail screen.
 */
sealed class DietUiState {
    /** Loading the plan detail from the repository.*/
    data object Loading : DietUiState(), UiStateHost.Loading

    /** Regenerating the plan after rejection or user feedback.*/
    data object Regenerating : DietUiState()

    /**
     * Error loading or processing the plan.
     *
     * @property message Error message to display to the user.
     */
    data class Error(override val message: String) : DietUiState(), UiStateHost.Error

    /**
     * Plan details uploaded correctly.
     *
     * @property plan Diet plan with days and meals.
     */
    data class Success(
        val plan: DietPlan,
    ) : DietUiState(), UiStateHost.Success
}

/**
 * UI state for the diet plan generation flow.
 */
sealed class GenerateDietUiState {
    /** No generation in progress; form ready to send.*/
    data object Idle : GenerateDietUiState()

    /** Generation in progress; loading animation is shown.*/
    data object Generating : GenerateDietUiState()

    /**
     * Error during generation.
     *
     * @property message Error message to display to the user.
     */
    data class Error(val message: String) : GenerateDietUiState()

    /**
     * Successfully generated plan.
     *
     * @property plan Newly created plan.
     */
    data class Success(val plan: DietPlan) : GenerateDietUiState()
}
