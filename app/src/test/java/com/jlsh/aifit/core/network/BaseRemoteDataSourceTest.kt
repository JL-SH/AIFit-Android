package com.jlsh.aifit.core.network

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import java.io.IOException

class BaseRemoteDataSourceTest {

    // Anonymous subclass so we can instantiate the open class
    private val sut = object : BaseRemoteDataSource() {}

    // ─── Helpers ───────────────────────────────────────────────────────────

    private fun errorRetrofitResponse(code: Int): retrofit2.Response<Unit> {
        val rawResponse = okhttp3.Response.Builder()
            .code(code)
            .message("HTTP $code")
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("http://test.com/api").build())
            .build()
        return retrofit2.Response.error(
            "".toResponseBody("application/json".toMediaTypeOrNull()),
            rawResponse,
        )
    }

    // ─── safeApiCall ───────────────────────────────────────────────────────

    @Test
    fun `safeApiCall returns Success when success=true and data non-null`() = runTest {
        val result = sut.safeApiCall { ApiResponse(success = true, data = "hello") }

        assertTrue(result is Result.Success)
        assertEquals("hello", (result as Result.Success).data)
    }

    @Test
    fun `safeApiCall returns Error when success=false`() = runTest {
        val result = sut.safeApiCall<String> {
            ApiResponse(success = false, message = "Plan not found")
        }

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.UnknownException)
    }

    @Test
    fun `safeApiCall error contains server message`() = runTest {
        val result = sut.safeApiCall<String> {
            ApiResponse(success = false, message = "Custom server error")
        }

        val exception = (result as Result.Error).exception as AppException.UnknownException
        assertEquals("Custom server error", exception.message)
    }

    @Test
    fun `safeApiCall returns Error when success=true but data is null`() = runTest {
        val result = sut.safeApiCall<String> { ApiResponse(success = true, data = null) }

        assertTrue(result is Result.Error)
    }

    @Test
    fun `safeApiCall maps IOException to NetworkException`() = runTest {
        val result = sut.safeApiCall<String> { throw IOException("no connection") }

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.NetworkException)
    }

    @Test
    fun `safeApiCall maps HTTP 401 to UnauthorizedException`() = runTest {
        val retrofitError = retrofit2.Response.error<ApiResponse<String>>(
            "".toResponseBody(null),
            okhttp3.Response.Builder()
                .code(401).message("Unauthorized")
                .protocol(Protocol.HTTP_1_1)
                .request(Request.Builder().url("http://test.com").build())
                .build(),
        )
        val result = sut.safeApiCall<String> { throw HttpException(retrofitError) }

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.UnauthorizedException)
    }

    @Test
    fun `safeApiCall maps success false with AI_OVERLOADED to AiOverloadedException`() = runTest {
        val result = sut.safeApiCall<String> {
            ApiResponse(
                success = false,
                data = null,
                message = "La IA está experimentando alta demanda en este momento.",
                errorCode = "AI_OVERLOADED",
            )
        }

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.AiOverloadedException)
    }

    @Test
    fun `safeApiCall maps HTTP 500 to ServerException`() = runTest {
        val retrofitError = retrofit2.Response.error<ApiResponse<String>>(
            "".toResponseBody(null),
            okhttp3.Response.Builder()
                .code(500).message("Server Error")
                .protocol(Protocol.HTTP_1_1)
                .request(Request.Builder().url("http://test.com").build())
                .build(),
        )
        val result = sut.safeApiCall<String> { throw HttpException(retrofitError) }

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ServerException)
    }

    // ─── safeEmptyApiCall ──────────────────────────────────────────────────

    @Test
    fun `safeEmptyApiCall returns Success on 204 response`() = runTest {
        val mockResponse = mockk<retrofit2.Response<Unit>> {
            every { isSuccessful } returns true
        }

        val result = sut.safeEmptyApiCall { mockResponse }

        assertTrue(result is Result.Success)
    }

    @Test
    fun `safeEmptyApiCall returns Error on 404 response`() = runTest {
        val errorResponse = errorRetrofitResponse(404)

        val result = sut.safeEmptyApiCall { errorResponse }

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.NotFoundException)
    }

    @Test
    fun `safeEmptyApiCall maps IOException to NetworkException`() = runTest {
        val result = sut.safeEmptyApiCall { throw IOException("timeout") }

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.NetworkException)
    }
}

