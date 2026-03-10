package com.jlsh.aifit.core.network

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)

open class BaseRemoteDataSource {

    suspend fun <T> safeApiCall(apiCall: suspend () -> ApiResponse<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(
                    AppException.UnknownException(
                        response.message ?: "Unknown server error"
                    )
                )
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorMapper.map(e))
        }
    }
}

