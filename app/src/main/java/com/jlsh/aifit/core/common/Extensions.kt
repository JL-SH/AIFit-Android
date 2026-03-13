package com.jlsh.aifit.core.common

fun AppException.toMessage(): String = when (this) {
    is AppException.NetworkException -> "Sin conexión. Comprueba tu internet."
    is AppException.UnauthorizedException -> "Sesión expirada. Vuelve a iniciar sesión."
    is AppException.ForbiddenException -> "No tienes permisos para realizar esta acción."
    is AppException.NotFoundException -> "No se encontró $resource."
    is AppException.ValidationException -> errors.values.firstOrNull() ?: "Datos inválidos."
    is AppException.ConflictException -> "El recurso ya existe o hay un conflicto."
    is AppException.ServerException -> "Error del servidor. Inténtalo más tarde."
    is AppException.UnknownException -> message.ifBlank { "Error inesperado. Inténtalo de nuevo." }
}

