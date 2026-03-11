package com.jlsh.aifit.feature.vision.domain.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.vision.domain.model.FoodPhotoAnalysisResult

interface VisionRepository {
    suspend fun analyzePhoto(imageBytes: ByteArray, contentType: String): Result<FoodPhotoAnalysisResult>
}

