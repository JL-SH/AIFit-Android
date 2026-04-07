package com.jlsh.aifit.feature.nutrition.ui.state

sealed class NutritionUiEvent {
    data class NavigateToTrackMeal(val mode: String) : NutritionUiEvent()
    data object NavigateToNutritionTarget : NutritionUiEvent()
    data class NavigateToDietDetail(val planId: String) : NutritionUiEvent()
    data object NavigateToGenerateDiet : NutritionUiEvent()
    data object ShowTrackMealSheet : NutritionUiEvent()
    data object NavigateBack : NutritionUiEvent()
    data object NavigateToHome : NutritionUiEvent()
    data class ShowSnackbar(val message: String) : NutritionUiEvent()
    data object MealDeleted : NutritionUiEvent()
    data class ShowAchievementDialog(val name: String, val description: String) : NutritionUiEvent()
}

