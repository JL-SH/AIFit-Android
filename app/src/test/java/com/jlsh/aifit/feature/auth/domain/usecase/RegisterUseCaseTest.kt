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

class RegisterUseCaseTest {

    private val repository: AuthRepository = mockk()
    private val sut = RegisterUseCase(repository)

    @Test
    fun `cuando el repositorio retorna Success, devuelve el mismo AuthToken`() = runTest {
        val expected = fakeAuthToken(profileComplete = false)
        coEvery { repository.register(any(), any(), any()) } returns Result.Success(expected)

        val result = sut("test@aifit.com", "password123", "Test User")

        assertTrue(result is Result.Success<*>)
        assertEquals(expected, (result as Result.Success).data)
    }

    @Test
    fun `cuando el repositorio retorna Error de conflicto, propaga el Error`() = runTest {
        coEvery { repository.register(any(), any(), any()) } returns
            Result.Error(AppException.ConflictException)

        val result = sut("test@aifit.com", "password123", "Test User")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ConflictException)
    }

    @Test
    fun `pasa correctamente email, password y nombre al repositorio`() = runTest {
        val email = "nuevo@aifit.com"
        val password = "pass1234"
        val name = "Nuevo Usuario"
        coEvery { repository.register(email, password, name) } returns Result.Success(fakeAuthToken())

        sut(email, password, name)

        coVerify { repository.register(email, password, name) }
    }
}
