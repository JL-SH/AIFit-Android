package com.jlsh.aifit.core.network

import com.jlsh.aifit.core.datastore.AuthDataStore
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private val authDataStore: AuthDataStore = mockk()
    private val chain: Interceptor.Chain = mockk()

    private val baseRequest = Request.Builder().url("https://aifit.com/api/profile").build()

    private val stubResponse: Response = Response.Builder()
        .code(200)
        .message("OK")
        .protocol(Protocol.HTTP_1_1)
        .request(baseRequest)
        .build()

    @Before
    fun setUp() {
        every { chain.request() } returns baseRequest
        every { chain.proceed(any()) } returns stubResponse
    }

    @Test
    fun `when token exists adds Authorization Bearer header`() {
        every { authDataStore.getToken() } returns "abc123token"
        val interceptor = AuthInterceptor(authDataStore)

        interceptor.intercept(chain)

        val capturedRequest = slot<Request>()
        verify { chain.proceed(capture(capturedRequest)) }
        assertEquals("Bearer abc123token", capturedRequest.captured.header("Authorization"))
    }

    @Test
    fun `when token is null proceeds without Authorization header`() {
        every { authDataStore.getToken() } returns null
        val interceptor = AuthInterceptor(authDataStore)

        interceptor.intercept(chain)

        val capturedRequest = slot<Request>()
        verify { chain.proceed(capture(capturedRequest)) }
        assertNull(capturedRequest.captured.header("Authorization"))
    }

    @Test
    fun `intercept always calls chain proceed exactly once`() {
        every { authDataStore.getToken() } returns "some-token"
        val interceptor = AuthInterceptor(authDataStore)

        interceptor.intercept(chain)

        verify(exactly = 1) { chain.proceed(any()) }
    }
}

