package com.jlsh.aifit.core.network

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import kotlinx.serialization.Serializable
import retrofit2.HttpException
import retrofit2.Response

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

    /** Use this for endpoints that return 204 No Content (no body to parse). */
    suspend fun safeEmptyApiCall(apiCall: suspend () -> Response<Unit>): Result<Unit> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(NetworkErrorMapper.map(HttpException(response)))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorMapper.map(e))
        }
    }
}

