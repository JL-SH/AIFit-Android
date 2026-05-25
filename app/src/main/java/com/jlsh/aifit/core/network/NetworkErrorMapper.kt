package com.jlsh.aifit.core.network

import com.jlsh.aifit.core.common.ApiErrorCode
import com.jlsh.aifit.core.common.AppException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.io.IOException

/**
 * Converts raw [Throwable] values from the network layer into typed
 * [AppException] subclasses that domain and UI code can handle without
 * depending directly on OkHttp or Retrofit types.
 */
object NetworkErrorMapper {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Maps a [Throwable] caught during a network call to a typed [AppException].
     *
     * | Input type      | Result                               |
     * |-----------------|--------------------------------------|
     * | [IOException]   | [AppException.NetworkException]      |
     * | [HttpException] | HTTP-status-specific subclass        |
     * | Anything else   | [AppException.UnknownException]      |
     *
     * @param throwable The raw exception thrown by Retrofit or OkHttp.
     * @return A typed [AppException] suitable for the domain and UI layers.
     */
    fun map(throwable: Throwable): AppException = when (throwable) {
        is IOException -> AppException.NetworkException
        is HttpException -> mapHttpException(throwable)
        else -> AppException.UnknownException(throwable.message ?: "Unknown error")
    }

    private fun mapHttpException(exception: HttpException): AppException {
        val parsed = parseApiErrorBody(exception)
        if (parsed?.errorCode == ApiErrorCode.AI_OVERLOADED) {
            return AppException.AiOverloadedException
        }
        return when (exception.code()) {
            400 -> parseValidationErrors(exception, parsed)
            401 -> AppException.UnauthorizedException
            403 -> AppException.ForbiddenException
            404 -> AppException.NotFoundException("Resource")
            409 -> AppException.ConflictException
            422 -> AppException.InsufficientDataException
            in 500..599 -> AppException.ServerException
            else -> AppException.UnknownException("HTTP ${exception.code()}")
        }
    }

    private fun parseValidationErrors(
        exception: HttpException,
        parsed: ParsedApiError?,
    ): AppException.ValidationException {
        if (parsed?.message != null) {
            return AppException.ValidationException(mapOf("general" to parsed.message))
        }
        return try {
            val errorBody = exception.response()?.errorBody()?.string() ?: ""
            val jsonElement = json.parseToJsonElement(errorBody)
            val messageStr = jsonElement.jsonObject["message"]?.jsonPrimitive?.content ?: ""
            AppException.ValidationException(mapOf("general" to messageStr))
        } catch (_: Exception) {
            AppException.ValidationException(emptyMap())
        }
    }

    private fun parseApiErrorBody(exception: HttpException): ParsedApiError? {
        return try {
            val errorBody = exception.response()?.errorBody()?.string() ?: return null
            if (errorBody.isBlank()) return null
            val jsonElement = json.parseToJsonElement(errorBody).jsonObject
            ParsedApiError(
                message = jsonElement["message"]?.jsonPrimitive?.content,
                errorCode = jsonElement["errorCode"]?.jsonPrimitive?.content,
            )
        } catch (_: Exception) {
            null
        }
    }

    private data class ParsedApiError(
        val message: String?,
        val errorCode: String?,
    )
}
