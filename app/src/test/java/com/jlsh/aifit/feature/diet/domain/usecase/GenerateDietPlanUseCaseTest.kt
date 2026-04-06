package com.jlsh.aifit.feature.diet.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.diet.data.dto.GenerateAdaptiveDietPlanRequestDto
import com.jlsh.aifit.feature.diet.domain.repository.DietRepository
import com.jlsh.aifit.testutil.fakeDietPlan
import com.jlsh.aifit.testutil.fakeGenerateDietPlanRequestDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GenerateDietPlanUseCaseTest {

    private val repository: DietRepository = mockk()
    private val sut = GenerateDietPlanUseCase(repository)

    @Test
    fun `invoke delega en repository y retorna plan generado`() = runTest {
        val plan = fakeDietPlan(id = "gen-diet")
        coEvery { repository.generateDietPlan(any()) } returns Result.Success(plan)

        val result = sut(fakeGenerateDietPlanRequestDto())

        assertTrue(result is Result.Success)
        assertEquals("gen-diet", (result as Result.Success).data.id)
    }

    @Test
    fun `invoke retorna Error cuando generación falla`() = runTest {
        coEvery { repository.generateDietPlan(any()) } returns
            Result.Error(AppException.ServerException)

        val result = sut(fakeGenerateDietPlanRequestDto())

        assertTrue(result is Result.Error)
    }

    @Test
    fun `invokeAdaptive delega en repository y retorna plan`() = runTest {
        val plan = fakeDietPlan(id = "adaptive-diet")
        coEvery { repository.generateAdaptiveDietPlan(any()) } returns Result.Success(plan)

        val request = GenerateAdaptiveDietPlanRequestDto(
            durationWeeks = 4, mealsPerDay = 3, dietPreference = "NONE",
        )
        val result = sut.invokeAdaptive(request)

        assertTrue(result is Result.Success)
        assertEquals("adaptive-diet", (result as Result.Success).data.id)
    }
}

