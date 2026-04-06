package com.jlsh.aifit.feature.user.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.user.domain.repository.UserRepository
import com.jlsh.aifit.testutil.fakeUserProfile
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetUserProfileUseCaseTest {

    private val repository: UserRepository = mockk()
    private val sut = GetUserProfileUseCase(repository)

    @Test
    fun `invoke delega en repository y retorna su Flow con Success`() = runTest {
        val profile = fakeUserProfile()
        every { repository.getProfile() } returns flowOf(
            Result.Loading,
            Result.Success(profile),
        )

        val emissions = sut().toList()

        assertTrue(emissions[0] is Result.Loading)
        assertTrue(emissions[1] is Result.Success<*>)
        assertEquals(profile, (emissions[1] as Result.Success<*>).data)
    }

    @Test
    fun `invoke delega en repository y propaga Error`() = runTest {
        every { repository.getProfile() } returns flowOf(
            Result.Error(AppException.NetworkException),
        )

        val emissions = sut().toList()

        assertTrue(emissions[0] is Result.Error)
        verify(exactly = 1) { repository.getProfile() }
    }
}
