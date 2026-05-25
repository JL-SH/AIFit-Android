package com.jlsh.aifit.core.network

import com.jlsh.aifit.core.common.ApiErrorCode
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import kotlinx.serialization.Serializable
import retrofit2.HttpException
import retrofit2.Response

/**
 * Generic wrapper around all JSON responses returned by the AIFit backend.
 *
 * @param T The type of the payload carried in [data].
 * @property success Indicates whether the operation succeeded on the server.
 * @property data Optional response payload; present when [success] is `true`.
 * @property message Optional human-readable message from the server,
 *   typically an error description when [success] is `false`.
 * @property errorCode Optional machine-readable code when [success] is `false`.
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean = false,
    val data: T? = null,
    val message: String? = null,
    val errorCode: String? = null,
)

/**
 * Base class for all remote data-source implementations.
 *
 * Provides [safeApiCall] and [safeEmptyApiCall] to execute Retrofit calls
 * inside a uniform try/catch that converts any exception into a typed
 * [Result.Error], so call-sites never need to catch raw network exceptions.
 */
open class BaseRemoteDataSource {

    /**
     * Executes [apiCall] and wraps the outcome in a [Result].
     *
     * Returns [Result.Success] when the server reports `success == true` and
     * provides a non-null [ApiResponse.data]. Returns [Result.Error] with an
     * [AppException] for any server-side failure or network error.
     *
     * @param T The expected payload type.
     * @param apiCall A suspend lambda that invokes the Retrofit endpoint and
     *   returns an [ApiResponse] wrapping type [T].
     * @return [Result.Success] with the unwrapped data, or [Result.Error].
     */
    suspend fun <T> safeApiCall(apiCall: suspend () -> ApiResponse<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(mapApiFailure(response.message, response.errorCode))
            }
        } catch (e: Exception) {
            Result.Error(NetworkErrorMapper.map(e))
        }
    }

    private fun mapApiFailure(message: String?, errorCode: String?): AppException =
        when (errorCode) {
            ApiErrorCode.AI_OVERLOADED -> AppException.AiOverloadedException
            else -> AppException.UnknownException(message ?: "Unknown server error")
        }

    /**
     * Executes [apiCall] for endpoints that return HTTP 204 No Content,
     * wrapping the outcome in a [Result].
     *
     * Returns [Result.Success] with [Unit] on any 2xx response and
     * [Result.Error] on any HTTP or network failure.
     *
     * @param apiCall A suspend lambda that calls the Retrofit endpoint and
     *   returns a raw [Response] of [Unit].
     * @return [Result.Success] on success, or [Result.Error] with an
     *   [AppException] on failure.
     */
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
