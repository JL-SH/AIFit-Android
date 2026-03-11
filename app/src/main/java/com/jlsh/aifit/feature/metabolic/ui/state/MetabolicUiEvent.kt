package com.jlsh.aifit.feature.metabolic.ui.state

sealed class MetabolicUiEvent {
    data object AdjustmentApplied : MetabolicUiEvent()
    data object NavigateBack : MetabolicUiEvent()
    data class ShowSnackbar(val message: String) : MetabolicUiEvent()
}

