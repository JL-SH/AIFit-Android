package com.jlsh.aifit.feature.metabolic.ui.state

import com.jlsh.aifit.core.ui.components.layout.UiStateHost
import com.jlsh.aifit.feature.metabolic.domain.model.MetabolicAnalysis
import com.jlsh.aifit.feature.metabolic.domain.model.MetabolicInsight

sealed class MetabolicUiState {
    data object Loading : MetabolicUiState(), UiStateHost.Loading
    data class Error(override val message: String) : MetabolicUiState(), UiStateHost.Error
    data class Success(
        val analysis: MetabolicAnalysis,
        val insights: List<MetabolicInsight>,
        val isApplying: Boolean = false,
    ) : MetabolicUiState(), UiStateHost.Success
}


