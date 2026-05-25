package com.jlsh.aifit.feature.vision.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.vision.domain.model.FoodPhotoAnalysisResult
import com.jlsh.aifit.feature.vision.domain.repository.VisionRepository
import javax.inject.Inject

/**
 * Use case that analyzes a food photo to identify foods and estimate macros.
 *
 * @param repository AI vision repository.
 */
class AnalyzeFoodPhotoUseCase @Inject constructor(
    private val repository: VisionRepository,
) {
    /**
     * Send the image to the analysis service and return the detected foods.
     *
     * @param imageBytes Binary content of the image (e.g. compressed JPEG).
     * @param contentType MIME type of the image; by default `image/jpeg`.
     * @return [Result.Success] with the result of the analysis, or [Result.Error] if the request fails.
     */
    suspend operator fun invoke(
        imageBytes: ByteArray,
        contentType: String = "image/jpeg",
    ): Result<FoodPhotoAnalysisResult> = repository.analyzePhoto(imageBytes, contentType)
}

