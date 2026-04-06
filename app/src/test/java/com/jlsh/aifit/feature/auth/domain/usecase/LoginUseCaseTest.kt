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

class LoginUseCaseTest {

    private val repository: AuthRepository = mockk()
    private val sut = LoginUseCase(repository)

    @Test
    fun `cuando el repositorio retorna Success, devuelve el mismo AuthToken`() = runTest {
        val expected = fakeAuthToken()
        coEvery { repository.login(any(), any()) } returns Result.Success(expected)

        val result = sut("test@aifit.com", "password123")

        assertTrue(result is Result.Success<*>)
        assertEquals(expected, (result as Result.Success).data)
        coVerify(exactly = 1) { repository.login("test@aifit.com", "password123") }
    }

    @Test
    fun `cuando el repositorio retorna Error de red, propaga el Error`() = runTest {
        coEvery { repository.login(any(), any()) } returns
            Result.Error(AppException.NetworkException)

        val result = sut("test@aifit.com", "password123")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.NetworkException)
    }

    @Test
    fun `cuando el repositorio retorna Error de servidor, propaga el Error`() = runTest {
        coEvery { repository.login(any(), any()) } returns
            Result.Error(AppException.ServerException)

        val result = sut("test@aifit.com", "password123")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ServerException)
    }

    @Test
    fun `pasa correctamente email y password al repositorio`() = runTest {
        val email = "usuario@test.com"
        val password = "clave_segura"
        coEvery { repository.login(email, password) } returns Result.Success(fakeAuthToken())

        sut(email, password)

        coVerify { repository.login(email, password) }
    }
}
