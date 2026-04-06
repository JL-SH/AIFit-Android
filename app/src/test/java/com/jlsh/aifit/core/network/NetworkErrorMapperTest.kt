package com.jlsh.aifit.core.network

import com.jlsh.aifit.core.common.AppException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import java.io.IOException

class NetworkErrorMapperTest {

    // ─── Helpers ───────────────────────────────────────────────────────────

    private fun httpException(code: Int, body: String = ""): HttpException {
        val rawResponse = okhttp3.Response.Builder()
            .code(code)
            .message("HTTP $code")
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("http://test.com/api").build())
            .body(body.toResponseBody("application/json".toMediaTypeOrNull()))
            .build()
        val retrofitResponse = retrofit2.Response.error<Unit>(
            body.toResponseBody("application/json".toMediaTypeOrNull()),
            rawResponse,
        )
        return HttpException(retrofitResponse)
    }

    // ─── IOException ───────────────────────────────────────────────────────

    @Test
    fun `IOException maps to NetworkException`() {
        val result = NetworkErrorMapper.map(IOException("timeout"))
        assertTrue(result is AppException.NetworkException)
    }

    // ─── HTTP status codes ─────────────────────────────────────────────────

    @Test
    fun `HTTP 400 maps to ValidationException`() {
        val result = NetworkErrorMapper.map(httpException(400))
        assertTrue(result is AppException.ValidationException)
    }

    @Test
    fun `HTTP 400 with JSON body extracts validation message`() {
        val body = """{"success":false,"message":"Email inválido"}"""
        val result = NetworkErrorMapper.map(httpException(400, body))
        assertTrue(result is AppException.ValidationException)
        val errors = (result as AppException.ValidationException).errors
        assertEquals("Email inválido", errors["general"])
    }

    @Test
    fun `HTTP 400 with invalid JSON body returns empty ValidationException`() {
        val result = NetworkErrorMapper.map(httpException(400, "not-json"))
        val exception = result as AppException.ValidationException
        assertTrue(exception.errors.isEmpty())
    }

    @Test
    fun `HTTP 401 maps to UnauthorizedException`() {
        val result = NetworkErrorMapper.map(httpException(401))
        assertTrue(result is AppException.UnauthorizedException)
    }

    @Test
    fun `HTTP 403 maps to ForbiddenException`() {
        val result = NetworkErrorMapper.map(httpException(403))
        assertTrue(result is AppException.ForbiddenException)
    }

    @Test
    fun `HTTP 404 maps to NotFoundException`() {
        val result = NetworkErrorMapper.map(httpException(404))
        assertTrue(result is AppException.NotFoundException)
    }

    @Test
    fun `HTTP 409 maps to ConflictException`() {
        val result = NetworkErrorMapper.map(httpException(409))
        assertTrue(result is AppException.ConflictException)
    }

    @Test
    fun `HTTP 500 maps to ServerException`() {
        val result = NetworkErrorMapper.map(httpException(500))
        assertTrue(result is AppException.ServerException)
    }

    @Test
    fun `HTTP 503 maps to ServerException`() {
        val result = NetworkErrorMapper.map(httpException(503))
        assertTrue(result is AppException.ServerException)
    }

    @Test
    fun `HTTP 418 unknown code maps to UnknownException`() {
        val result = NetworkErrorMapper.map(httpException(418))
        assertTrue(result is AppException.UnknownException)
    }

    // ─── Unknown Throwable ─────────────────────────────────────────────────

    @Test
    fun `arbitrary Throwable maps to UnknownException with its message`() {
        val result = NetworkErrorMapper.map(RuntimeException("something weird"))
        assertTrue(result is AppException.UnknownException)
        assertEquals("something weird", (result as AppException.UnknownException).message)
    }
}

