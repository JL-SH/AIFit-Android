package com.jlsh.aifit.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppExceptionExtensionsTest {

    // ─── NetworkException ──────────────────────────────────────────────────

    @Test
    fun `NetworkException toMessage contains connectivity hint`() {
        val result = AppException.NetworkException.toMessage()
        assertEquals("Sin conexión. Comprueba tu internet.", result)
    }

    // ─── UnauthorizedException ─────────────────────────────────────────────

    @Test
    fun `UnauthorizedException toMessage says session expired`() {
        val result = AppException.UnauthorizedException.toMessage()
        assertEquals("Sesión expirada. Vuelve a iniciar sesión.", result)
    }

    // ─── ForbiddenException ────────────────────────────────────────────────

    @Test
    fun `ForbiddenException toMessage says no permissions`() {
        val result = AppException.ForbiddenException.toMessage()
        assertEquals("No tienes permisos para realizar esta acción.", result)
    }

    // ─── NotFoundException ─────────────────────────────────────────────────

    @Test
    fun `NotFoundException toMessage includes resource name`() {
        val result = AppException.NotFoundException("Plan").toMessage()
        assertTrue("Message should include resource name 'Plan'", result.contains("Plan"))
    }

    // ─── ValidationException ──────────────────────────────────────────────

    @Test
    fun `ValidationException toMessage returns first error value`() {
        val ex = AppException.ValidationException(mapOf("email" to "Email inválido"))
        assertEquals("Email inválido", ex.toMessage())
    }

    @Test
    fun `ValidationException with empty map returns generic invalid data message`() {
        val ex = AppException.ValidationException(emptyMap())
        assertEquals("Datos inválidos.", ex.toMessage())
    }

    // ─── ConflictException ─────────────────────────────────────────────────

    @Test
    fun `ConflictException toMessage is non-blank`() {
        val result = AppException.ConflictException.toMessage()
        assertFalse("ConflictException message must not be blank", result.isBlank())
    }

    // ─── ServerException ───────────────────────────────────────────────────

    @Test
    fun `ServerException toMessage is non-blank`() {
        val result = AppException.ServerException.toMessage()
        assertFalse("ServerException message must not be blank", result.isBlank())
    }

    // ─── UnknownException ──────────────────────────────────────────────────

    @Test
    fun `UnknownException toMessage returns exception message when non-blank`() {
        val result = AppException.UnknownException("Custom error detail").toMessage()
        assertEquals("Custom error detail", result)
    }

    @Test
    fun `UnknownException toMessage returns fallback when message is blank`() {
        val result = AppException.UnknownException("   ").toMessage()
        assertFalse("Fallback message must not be blank", result.isBlank())
    }
}

