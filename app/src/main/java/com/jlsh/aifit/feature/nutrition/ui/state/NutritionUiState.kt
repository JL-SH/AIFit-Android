package com.jlsh.aifit.feature.nutrition.ui.state

import com.jlsh.aifit.core.ui.components.layout.UiStateHost
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionLog
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget

data class TodayState(
    val nutritionLog: NutritionLog? = null,
    val target: NutritionTarget? = null,
)

sealed class NutritionHubUiState {
    data object Loading : NutritionHubUiState(), UiStateHost.Loading
    data class Error(override val message: String) : NutritionHubUiState(), UiStateHost.Error
    data class Success(
        val todayState: TodayState,
        val dietPlans: List<DietPlan>,
        val selectedTabIndex: Int = 0,
    ) : NutritionHubUiState(), UiStateHost.Success
}

sealed class TrackMealUiState {
    data object Idle : TrackMealUiState()
    data object Saving : TrackMealUiState()
    data object Analyzing : TrackMealUiState()
    data class Error(val message: String) : TrackMealUiState()
    data object Saved : TrackMealUiState()
}

sealed class NutritionTargetUiState {
    data object Loading : NutritionTargetUiState()
    data class Error(val message: String) : NutritionTargetUiState()
    data class Ready(
        val calorieTarget: String,
        val proteinTarget: String,
        val carbsTarget: String,
        val fatTarget: String,
        val setBy: String,
        val isSaving: Boolean = false,
    ) : NutritionTargetUiState()
}

