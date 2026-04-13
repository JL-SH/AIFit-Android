package com.jlsh.aifit.core.common

sealed class AppException(override val message: String) : Exception(message) {
    data class ValidationException(val errors: Map<String, String>) :
        AppException("Validation failed")

    data object UnauthorizedException : AppException("Unauthorized")
    data object ForbiddenException : AppException("Forbidden")
    data class NotFoundException(val resource: String) : AppException("$resource not found")
    data object ConflictException : AppException("Conflict")
    data object ServerException : AppException("Server error")
    data object NetworkException : AppException("Network error")
    data object InsufficientDataException : AppException("Insufficient data")
    data class UnknownException(override val message: String) : AppException(message)
}

