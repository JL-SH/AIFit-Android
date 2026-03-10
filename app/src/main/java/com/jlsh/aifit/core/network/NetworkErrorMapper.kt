package com.jlsh.aifit.core.network

import com.jlsh.aifit.core.common.AppException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.io.IOException

object NetworkErrorMapper {

    private val json = Json { ignoreUnknownKeys = true }

    fun map(throwable: Throwable): AppException = when (throwable) {
        is IOException -> AppException.NetworkException
        is HttpException -> mapHttpException(throwable)
        else -> AppException.UnknownException(throwable.message ?: "Unknown error")
    }

    private fun mapHttpException(exception: HttpException): AppException =
        when (exception.code()) {
            400 -> parseValidationErrors(exception)
            401 -> AppException.UnauthorizedException
            403 -> AppException.ForbiddenException
            404 -> AppException.NotFoundException("Resource")
            409 -> AppException.ConflictException
            in 500..599 -> AppException.ServerException
            else -> AppException.UnknownException("HTTP ${exception.code()}")
        }

    private fun parseValidationErrors(exception: HttpException): AppException.ValidationException {
        return try {
            val errorBody = exception.response()?.errorBody()?.string() ?: ""
            val jsonElement = json.parseToJsonElement(errorBody)
            val messageStr = jsonElement.jsonObject["message"]?.jsonPrimitive?.content ?: ""
            AppException.ValidationException(mapOf("general" to messageStr))
        } catch (_: Exception) {
            AppException.ValidationException(emptyMap())
        }
    }
}

