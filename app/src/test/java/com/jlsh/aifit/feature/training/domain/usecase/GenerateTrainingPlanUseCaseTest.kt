package com.jlsh.aifit.feature.training.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.training.data.dto.GenerateAdaptiveTrainingPlanRequestDto
import com.jlsh.aifit.feature.training.domain.repository.TrainingRepository
import com.jlsh.aifit.testutil.fakeGenerateTrainingPlanRequestDto
import com.jlsh.aifit.testutil.fakeTrainingPlan
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GenerateTrainingPlanUseCaseTest {

    private val repository: TrainingRepository = mockk()
    private val sut = GenerateTrainingPlanUseCase(repository)

    @Test
    fun `invoke delega en repository y retorna plan generado`() = runTest {
        val plan = fakeTrainingPlan(id = "new-plan")
        coEvery { repository.generateTrainingPlan(any()) } returns Result.Success(plan)

        val result = sut(fakeGenerateTrainingPlanRequestDto())

        assertTrue(result is Result.Success)
        assertEquals("new-plan", (result as Result.Success).data.id)
    }

    @Test
    fun `invoke retorna Error cuando generación falla`() = runTest {
        coEvery { repository.generateTrainingPlan(any()) } returns
            Result.Error(AppException.ServerException)

        val result = sut(fakeGenerateTrainingPlanRequestDto())

        assertTrue(result is Result.Error)
    }

    @Test
    fun `invokeAdaptive delega en repository y retorna plan`() = runTest {
        val plan = fakeTrainingPlan(id = "adaptive-plan")
        coEvery { repository.generateAdaptiveTrainingPlan(any()) } returns Result.Success(plan)

        val request = GenerateAdaptiveTrainingPlanRequestDto(
            frequencyDaysPerWeek = 4, sessionDurationMinutes = 60,
            durationWeeks = 8, goalType = "GAIN_MUSCLE",
            fitnessLevel = "INTERMEDIATE", location = "GYM",
        )
        val result = sut.invokeAdaptive(request)

        assertTrue(result is Result.Success)
        assertEquals("adaptive-plan", (result as Result.Success).data.id)
    }
}

