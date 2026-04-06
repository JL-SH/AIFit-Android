package com.jlsh.aifit.feature.auth.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.auth.domain.repository.AuthRepository
import com.jlsh.aifit.testutil.fakeAuthToken
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GoogleLoginUseCaseTest {

    private val repository: AuthRepository = mockk()
    private val sut = GoogleLoginUseCase(repository)

    @Test
    fun `cuando el repositorio retorna Success, devuelve el mismo AuthToken`() = runTest {
        val expected = fakeAuthToken()
        coEvery { repository.googleLogin(any()) } returns Result.Success(expected)

        val result = sut("google-id-token-abc123")

        assertTrue(result is Result.Success<*>)
        assertEquals(expected, (result as Result.Success).data)
    }

    @Test
    fun `cuando el repositorio retorna Error no autorizado, propaga el Error`() = runTest {
        coEvery { repository.googleLogin(any()) } returns
            Result.Error(AppException.UnauthorizedException)

        val result = sut("invalid-token")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.UnauthorizedException)
    }

    @Test
    fun `pasa correctamente el idToken al repositorio`() = runTest {
        val idToken = "my-google-id-token"
        coEvery { repository.googleLogin(idToken) } returns Result.Success(fakeAuthToken())

        sut(idToken)

        coVerify { repository.googleLogin(idToken) }
    }
}
