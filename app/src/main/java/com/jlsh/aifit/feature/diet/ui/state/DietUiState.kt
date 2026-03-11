package com.jlsh.aifit.feature.diet.ui.state

import com.jlsh.aifit.core.ui.components.layout.UiStateHost
import com.jlsh.aifit.feature.diet.domain.model.DietPlan

sealed class DietUiState {
    data object Loading : DietUiState(), UiStateHost.Loading
    data class Error(override val message: String) : DietUiState(), UiStateHost.Error
    data class Success(
        val plan: DietPlan,
    ) : DietUiState(), UiStateHost.Success
}

sealed class GenerateDietUiState {
    data object Idle : GenerateDietUiState()
    data object Generating : GenerateDietUiState()
    data class Error(val message: String) : GenerateDietUiState()
    data class Success(val plan: DietPlan) : GenerateDietUiState()
}

