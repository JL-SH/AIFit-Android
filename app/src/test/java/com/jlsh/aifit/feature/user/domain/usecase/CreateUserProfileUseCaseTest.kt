package com.jlsh.aifit.feature.user.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.user.domain.repository.UserRepository
import com.jlsh.aifit.testutil.fakeCreateUserProfileRequest
import com.jlsh.aifit.testutil.fakeUserProfile
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateUserProfileUseCaseTest {

    private val repository: UserRepository = mockk()
    private val sut = CreateUserProfileUseCase(repository)

    @Test
    fun `invoke happy path retorna Success con el perfil creado`() = runTest {
        val profile = fakeUserProfile()
        val request = fakeCreateUserProfileRequest()
        coEvery { repository.createProfile(request) } returns Result.Success(profile)

        val result = sut(request)

        assertTrue(result is Result.Success<*>)
        assertEquals(profile, (result as Result.Success<*>).data)
    }

    @Test
    fun `invoke cuando falla el repositorio retorna Error`() = runTest {
        val request = fakeCreateUserProfileRequest()
        coEvery { repository.createProfile(request) } returns
            Result.Error(AppException.ServerException)

        val result = sut(request)

        assertTrue(result is Result.Error)
    }

    @Test
    fun `invoke delega exactamente al repositorio con el mismo request`() = runTest {
        val request = fakeCreateUserProfileRequest()
        coEvery { repository.createProfile(any()) } returns Result.Success(fakeUserProfile())

        sut(request)

        coVerify(exactly = 1) { repository.createProfile(request) }
    }
}
