package com.jlsh.aifit.core.common

/**
 * Converts an [AppException] into a user-facing, localised Spanish string
 * suitable for display in a Snackbar or error label.
 *
 * Each subclass maps to a distinct message. [AppException.UnknownException]
 * falls back to its own [AppException.message] when non-blank, or to a
 * generic "unexpected error" string otherwise.
 *
 * @receiver The [AppException] to convert.
 * @return A non-empty, human-readable error string in Spanish.
 */
fun AppException.toMessage(): String = when (this) {
    is AppException.NetworkException -> "Sin conexión. Comprueba tu internet."
    is AppException.UnauthorizedException -> "Sesión expirada. Vuelve a iniciar sesión."
    is AppException.ForbiddenException -> "No tienes permisos para realizar esta acción."
    is AppException.NotFoundException -> "No se encontró $resource."
    is AppException.ValidationException -> errors.values.firstOrNull() ?: "Datos inválidos."
    is AppException.ConflictException -> "El recurso ya existe o hay un conflicto."
    is AppException.ServerException -> "Error del servidor. Inténtalo más tarde."
    is AppException.UnknownException -> message.ifBlank { "Error inesperado. Inténtalo de nuevo." }
    is AppException.InsufficientDataException -> "Necesitas más datos para realizar este análisis. Registra al menos 2 semanas de peso y entrenamientos."
}
