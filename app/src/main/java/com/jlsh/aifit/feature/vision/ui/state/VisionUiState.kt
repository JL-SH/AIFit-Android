package com.jlsh.aifit.feature.vision.ui.state

import com.jlsh.aifit.feature.vision.domain.model.FoodPhotoAnalysisResult

sealed class VisionUiState {
    data object Idle : VisionUiState()
    data object Analyzing : VisionUiState()
    data class Result(val result: FoodPhotoAnalysisResult) : VisionUiState()
    data class Error(val message: String) : VisionUiState()
}

