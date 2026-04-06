package com.jlsh.aifit.feature.auth.data.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.core.session.SessionManager
import com.jlsh.aifit.feature.auth.data.api.AuthApiService
import com.jlsh.aifit.feature.auth.domain.model.AuthToken
import com.jlsh.aifit.testutil.FAKE_EMAIL
import com.jlsh.aifit.testutil.FAKE_NAME
import com.jlsh.aifit.testutil.FAKE_TOKEN
import com.jlsh.aifit.testutil.FAKE_USER_ID
import com.jlsh.aifit.testutil.fakeAuthResponseDto
import io.mockk.coEvery
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class AuthRepositoryImplTest {

    private val apiService: AuthApiService = mockk()
    private val sessionManager: SessionManager = mockk()
    private lateinit var sut: AuthRepositoryImpl

    @Before
    fun setUp() {
        sut = AuthRepositoryImpl(apiService, sessionManager)
    }

    // ─── login ────────────────────────────────────────────────────────────────

    @Test
    fun `login happy path - retorna AuthToken y llama a sessionManager`() = runTest {
        val dto = fakeAuthResponseDto()
        coEvery { apiService.login(any()) } returns ApiResponse(success = true, data = dto)
        justRun { sessionManager.onLoginSuccess(any(), any(), any(), any(), any()) }

        val result = sut.login(FAKE_EMAIL, "pass123")

        assertTrue(result is Result.Success<*>)
        val token = (result as Result.Success<AuthToken>).data
        assertEquals(dto.token, token.token)
        assertEquals(dto.userId, token.userId)
        assertEquals(dto.profileComplete, token.profileComplete)
        verify(exactly = 1) {
            sessionManager.onLoginSuccess(FAKE_TOKEN, FAKE_USER_ID, FAKE_EMAIL, FAKE_NAME, true)
        }
    }

    @Test
    fun `login cuando API lanza IOException - retorna Error y no llama a sessionManager`() = runTest {
        coEvery { apiService.login(any()) } throws IOException("Sin conexion")

        val result = sut.login(FAKE_EMAIL, "pass123")

        assertTrue(result is Result.Error)
        verify(exactly = 0) { sessionManager.onLoginSuccess(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `login cuando API retorna success=false - retorna Error y no llama a sessionManager`() = runTest {
        coEvery { apiService.login(any()) } returns
            ApiResponse(success = false, data = null, message = "Credenciales invalidas")

        val result = sut.login(FAKE_EMAIL, "wrongpass")

        assertTrue(result is Result.Error)
        verify(exactly = 0) { sessionManager.onLoginSuccess(any(), any(), any(), any(), any()) }
    }

    // ─── register ─────────────────────────────────────────────────────────────

    @Test
    fun `register happy path - retorna AuthToken con profileComplete=false`() = runTest {
        val dto = fakeAuthResponseDto(profileComplete = false)
        coEvery { apiService.register(any()) } returns ApiResponse(success = true, data = dto)
        justRun { sessionManager.onLoginSuccess(any(), any(), any(), any(), any()) }

        val result = sut.register(FAKE_EMAIL, "pass123", FAKE_NAME)

        assertTrue(result is Result.Success<*>)
        assertEquals(false, (result as Result.Success<AuthToken>).data.profileComplete)
    }

    @Test
    fun `register cuando API lanza IOException - retorna Error`() = runTest {
        coEvery { apiService.register(any()) } throws IOException("timeout")

        val result = sut.register(FAKE_EMAIL, "pass123", FAKE_NAME)

        assertTrue(result is Result.Error)
    }

    @Test
    fun `register cuando email duplicado - retorna Error`() = runTest {
        coEvery { apiService.register(any()) } returns
            ApiResponse(success = false, data = null, message = "Email ya registrado")

        val result = sut.register(FAKE_EMAIL, "pass123", FAKE_NAME)

        assertTrue(result is Result.Error)
        verify(exactly = 0) { sessionManager.onLoginSuccess(any(), any(), any(), any(), any()) }
    }

    // ─── googleLogin ──────────────────────────────────────────────────────────

    @Test
    fun `googleLogin happy path - retorna AuthToken y llama a sessionManager`() = runTest {
        val dto = fakeAuthResponseDto()
        coEvery { apiService.googleLogin(any()) } returns ApiResponse(success = true, data = dto)
        justRun { sessionManager.onLoginSuccess(any(), any(), any(), any(), any()) }

        val result = sut.googleLogin("google-id-token")

        assertTrue(result is Result.Success<*>)
        verify(exactly = 1) { sessionManager.onLoginSuccess(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `googleLogin cuando token invalido - retorna Error`() = runTest {
        coEvery { apiService.googleLogin(any()) } throws IOException("red no disponible")

        val result = sut.googleLogin("bad-token")

        assertTrue(result is Result.Error)
    }
}
