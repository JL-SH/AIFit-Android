package com.jlsh.aifit.core.common

/**
 * Sealed hierarchy of typed exceptions used across the domain and data layers.
 *
 * Every network, persistence, or business-rule failure is mapped to one of
 * these subclasses so the UI can react with specific, localised messages
 * (see [AppException.toMessage]) rather than catching raw exceptions.
 *
 * @property message A developer-facing description of the error.
 */
sealed class AppException(override val message: String) : Exception(message) {

    /**
     * One or more input fields failed server-side validation.
     *
     * @property errors Map of field names to human-readable error messages.
     */
    data class ValidationException(val errors: Map<String, String>) :
        AppException("Validation failed")

    /** The request was rejected because the JWT is missing or expired (HTTP 401). */
    data object UnauthorizedException : AppException("Unauthorized")

    /** The authenticated user lacks the required permissions (HTTP 403). */
    data object ForbiddenException : AppException("Forbidden")

    /**
     * The requested resource does not exist (HTTP 404).
     *
     * @property resource A human-readable name of the missing resource.
     */
    data class NotFoundException(val resource: String) : AppException("$resource not found")

    /** A write operation conflicted with existing server-side data (HTTP 409). */
    data object ConflictException : AppException("Conflict")

    /** The backend returned an HTTP 5xx response. */
    data object ServerException : AppException("Server error")

    /** The device has no network connectivity or the connection timed out. */
    data object NetworkException : AppException("Network error")

    /** The operation requires more user data than is currently available (HTTP 422). */
    data object InsufficientDataException : AppException("Insufficient data")

    /**
     * A catch-all for errors that do not map to any specific subclass.
     *
     * @property message A developer-facing description of the unexpected error.
     */
    data class UnknownException(override val message: String) : AppException(message)
}
