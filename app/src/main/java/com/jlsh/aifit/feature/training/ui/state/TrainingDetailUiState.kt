package com.jlsh.aifit.feature.training.ui.state

import com.jlsh.aifit.core.ui.components.layout.UiStateHost

sealed interface TrainingDetailUiState {
    data object Loading : TrainingDetailUiState, UiStateHost.Loading
    data object Regenerating : TrainingDetailUiState
    data class Error(override val message: String) : TrainingDetailUiState, UiStateHost.Error
    data class Ready(val planName: String, val days: List<TrainingDayItem>) : TrainingDetailUiState, UiStateHost.Success
}

