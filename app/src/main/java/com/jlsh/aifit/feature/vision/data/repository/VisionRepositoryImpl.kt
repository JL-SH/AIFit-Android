package com.jlsh.aifit.feature.vision.data.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.feature.vision.data.api.VisionApiService
import com.jlsh.aifit.feature.vision.data.mapper.VisionMapper.toDomain
import com.jlsh.aifit.feature.vision.domain.model.FoodPhotoAnalysisResult
import com.jlsh.aifit.feature.vision.domain.repository.VisionRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class VisionRepositoryImpl @Inject constructor(
    private val apiService: VisionApiService,
) : BaseRemoteDataSource(), VisionRepository {

    override suspend fun analyzePhoto(
        imageBytes: ByteArray,
        contentType: String,
    ): Result<FoodPhotoAnalysisResult> {
        val requestBody = imageBytes.toRequestBody(contentType.toMediaType())
        val part = MultipartBody.Part.createFormData("image", "photo.jpg", requestBody)
        return when (val r = safeApiCall { apiService.analyzePhoto(part) }) {
            is Result.Success -> Result.Success(r.data.toDomain())
            is Result.Error -> r
            else -> Result.Loading
        }
    }
}

