package com.jlsh.aifit.feature.vision.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.vision.domain.model.FoodPhotoAnalysisResult
import com.jlsh.aifit.feature.vision.domain.repository.VisionRepository
import javax.inject.Inject

class AnalyzeFoodPhotoUseCase @Inject constructor(
    private val repository: VisionRepository,
) {
    suspend operator fun invoke(
        imageBytes: ByteArray,
        contentType: String = "image/jpeg",
    ): Result<FoodPhotoAnalysisResult> = repository.analyzePhoto(imageBytes, contentType)
}

