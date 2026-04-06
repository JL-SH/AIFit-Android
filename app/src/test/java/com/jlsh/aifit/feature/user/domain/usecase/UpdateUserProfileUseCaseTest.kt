package com.jlsh.aifit.feature.user.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.user.domain.repository.UserRepository
import com.jlsh.aifit.testutil.fakeUpdateUserProfileRequest
import com.jlsh.aifit.testutil.fakeUserProfile
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateUserProfileUseCaseTest {

    private val repository: UserRepository = mockk()
    private val sut = UpdateUserProfileUseCase(repository)

    @Test
    fun `invoke happy path retorna Success con el perfil actualizado`() = runTest {
        val profile = fakeUserProfile()
        val request = fakeUpdateUserProfileRequest()
        coEvery { repository.updateProfile(request) } returns Result.Success(profile)

        val result = sut(request)

        assertTrue(result is Result.Success<*>)
        assertEquals(profile, (result as Result.Success<*>).data)
    }

    @Test
    fun `invoke cuando falla el repositorio retorna Error`() = runTest {
        val request = fakeUpdateUserProfileRequest()
        coEvery { repository.updateProfile(request) } returns
            Result.Error(AppException.NetworkException)

        val result = sut(request)

        assertTrue(result is Result.Error)
    }

    @Test
    fun `invoke delega exactamente al repositorio con el mismo request`() = runTest {
        val request = fakeUpdateUserProfileRequest()
        coEvery { repository.updateProfile(any()) } returns Result.Success(fakeUserProfile())

        sut(request)

        coVerify(exactly = 1) { repository.updateProfile(request) }
    }
}
