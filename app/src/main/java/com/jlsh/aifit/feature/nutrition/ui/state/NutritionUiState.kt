package com.jlsh.aifit.feature.nutrition.ui.state

import com.jlsh.aifit.core.ui.components.layout.UiStateHost
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionLog
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget
import com.jlsh.aifit.feature.training.domain.model.PlanStatus

/**
 * Data for the current day for the "Today" tab of the nutrition hub.
 *
 * @property nutritionLog Log of meals for the day; null if there are no entries yet.
 * @property target Current calorie and macro goals; null if they are not set.
 */
data class TodayState(
    val nutritionLog: NutritionLog? = null,
    val target: NutritionTarget? = null,
)

/**
 * Nutrition hub UI status (Today, Diet Plan, and Shopping tabs).
 */
sealed class NutritionHubUiState {
    /** Loading log of the day, goals and list of diet plans.*/
    data object Loading : NutritionHubUiState(), UiStateHost.Loading

    /**
     * Error loading data from hub.
     *
     * @property message Error message to display to the user.
     */
    data class Error(override val message: String) : NutritionHubUiState(), UiStateHost.Error

    /**
     * Hub loaded with data for the day and diet plans.
     *
     * @property todayState Nutrition summary and goals for the current day.
     * @property dietPlans List of user's diet plans.
     * @property selectedTabIndex Index of the selected tab (0 = Today, 1 = Plan, 2 = Purchases).
     * @property selectedDietPlanFilter Filter by plan status; null shows all.
     * @property isActivatingPlan true while confirming activation of a plan on the server.
     */
    data class Success(
        val todayState: TodayState,
        val dietPlans: List<DietPlan>,
        val selectedTabIndex: Int = 0,
        val selectedDietPlanFilter: PlanStatus? = null,
        val isActivatingPlan: Boolean = false,
    ) : NutritionHubUiState(), UiStateHost.Success
}

/**
 * UI status for recording or analyzing a meal.
 */
sealed class TrackMealUiState {
    /** Form ready; no operation in progress.*/
    data object Idle : TrackMealUiState()

    /** Saving manual food on the server.*/
    data object Saving : TrackMealUiState()

    /** Analyzing text with AI before registering food.*/
    data object Analyzing : TrackMealUiState()

    /**
     * Error saving or parsing.
     *
     * @property message Error message to display to the user.
     */
    data class Error(val message: String) : TrackMealUiState()

    /** Food recorded correctly; the UI can navigate to the hub.*/
    data object Saved : TrackMealUiState()
}

/**
 * UI status to edit the user's nutritional goals.
 */
sealed class NutritionTargetUiState {
    /** Loading current objectives.*/
    data object Loading : NutritionTargetUiState()

    /**
     * Error loading targets.
     *
     * @property message Error message to display to the user.
     */
    data class Error(val message: String) : NutritionTargetUiState()

    /**
     * Objectives ready to edit in form.
     *
     * @property calorieTarget Calorie target as editable text.
     * @property proteinTarget Protein target (g) as editable text.
     * @property carbsTarget Target carbs (g) as editable text.
     * @property fatTarget Fat target (g) as editable text.
     * @property setBy Source of the goal (manual, diet plan, etc.) to display to the user.
     * @property isSaving true while the update is persisted on the server.
     */
    data class Ready(
        val calorieTarget: String,
        val proteinTarget: String,
        val carbsTarget: String,
        val fatTarget: String,
        val setBy: String,
        val isSaving: Boolean = false,
    ) : NutritionTargetUiState()
}
