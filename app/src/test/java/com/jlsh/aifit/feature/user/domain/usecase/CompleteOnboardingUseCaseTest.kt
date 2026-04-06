package com.jlsh.aifit.feature.user.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.user.domain.model.OnboardingResult
import com.jlsh.aifit.feature.user.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class CompleteOnboardingUseCaseTest {

    private val repository: UserRepository = mockk()
    private val sut = CompleteOnboardingUseCase(repository)

    @Test
    fun `invoke sin feedback delega en repository y retorna su resultado`() = runTest {
        val expected: Result<OnboardingResult> = mockk()
        coEvery { repository.completeOnboarding(null) } returns expected

        val actual = sut(feedback = null)

        assertTrue(actual === expected)
        coVerify(exactly = 1) { repository.completeOnboarding(null) }
    }

    @Test
    fun `invoke con feedback lo pasa al repositorio`() = runTest {
        val expected: Result<OnboardingResult> = mockk()
        coEvery { repository.completeOnboarding("Sin lesiones") } returns expected

        val actual = sut(feedback = "Sin lesiones")

        assertTrue(actual === expected)
        coVerify { repository.completeOnboarding("Sin lesiones") }
    }

    @Test
    fun `invoke cuando repository falla retorna Error`() = runTest {
        coEvery { repository.completeOnboarding(any()) } returns
            Result.Error(AppException.ServerException)

        val result = sut()

        assertTrue(result is Result.Error)
    }
}
