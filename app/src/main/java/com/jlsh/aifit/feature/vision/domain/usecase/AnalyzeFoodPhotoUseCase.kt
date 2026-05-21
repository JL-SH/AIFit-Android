package com.jlsh.aifit.feature.vision.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.vision.domain.model.FoodPhotoAnalysisResult
import com.jlsh.aifit.feature.vision.domain.repository.VisionRepository
import javax.inject.Inject

/**
 * Caso de uso que analiza una foto de comida para identificar alimentos y estimar macros.
 *
 * @param repository Repositorio de visión por IA.
 */
class AnalyzeFoodPhotoUseCase @Inject constructor(
    private val repository: VisionRepository,
) {
    /**
     * Envía la imagen al servicio de análisis y devuelve los alimentos detectados.
     *
     * @param imageBytes Contenido binario de la imagen (p. ej. JPEG comprimido).
     * @param contentType Tipo MIME de la imagen; por defecto `image/jpeg`.
     * @return [Result.Success] con el resultado del análisis, o [Result.Error] si falla la petición.
     */
    suspend operator fun invoke(
        imageBytes: ByteArray,
        contentType: String = "image/jpeg",
    ): Result<FoodPhotoAnalysisResult> = repository.analyzePhoto(imageBytes, contentType)
}

