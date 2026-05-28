package com.jlsh.aifit.feature.education.data.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.education.data.api.EducationApiService
import com.jlsh.aifit.testutil.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class EducationRepositoryImplTest {

    private val apiService: EducationApiService = mockk()
    private val repository = EducationRepositoryImpl(apiService)

    @Before
    fun setUp() {
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0
        every { android.util.Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(android.util.Log::class)
    }

    // ─── getExerciseExplanation ────────────────────────────────────────────────

    @Test
    fun `getExerciseExplanation cuando API responde OK retorna Success`() = runTest {
        coEvery { apiService.getExerciseExplanation(any()) } returns ApiResponse(
            success = true, data = fakeContextualExplanationResponseDto(),
        )

        val result = repository.getExerciseExplanation("exercise-1")

        assertTrue(result is Result.Success)
        assertEquals("expl-1", (result as Result.Success).data.id)
    }

    @Test
    fun `getExerciseExplanation cuando API falla retorna Error`() = runTest {
        coEvery { apiService.getExerciseExplanation(any()) } throws Exception("Network error")

        val result = repository.getExerciseExplanation("exercise-1")

        assertTrue(result is Result.Error)
    }

    // ─── getMealExplanation ────────────────────────────────────────────────────

    @Test
    fun `getMealExplanation cuando API responde OK retorna Success`() = runTest {
        coEvery { apiService.getMealExplanation(any()) } returns ApiResponse(
            success = true, data = fakeContextualExplanationResponseDto(referenceType = "MEAL"),
        )

        val result = repository.getMealExplanation("meal-1")

        assertTrue(result is Result.Success)
    }

    @Test
    fun `getMealExplanation cuando API falla retorna Error`() = runTest {
        coEvery { apiService.getMealExplanation(any()) } throws Exception("Network error")

        val result = repository.getMealExplanation("meal-1")

        assertTrue(result is Result.Error)
    }

    // ─── getWhyThisExercise ────────────────────────────────────────────────────

    @Test
    fun `getWhyThisExercise cuando API responde OK retorna Success`() = runTest {
        coEvery { apiService.getWhyThisExercise(any()) } returns ApiResponse(
            success = true, data = fakeWhyThisResponseDto(),
        )

        val result = repository.getWhyThisExercise("exercise-1")

        assertTrue(result is Result.Success)
        assertEquals("exercise-1", (result as Result.Success).data.referenceId)
    }

    @Test
    fun `getWhyThisExercise cuando API falla retorna Error`() = runTest {
        coEvery { apiService.getWhyThisExercise(any()) } throws Exception("Network error")

        val result = repository.getWhyThisExercise("exercise-1")

        assertTrue(result is Result.Error)
    }

    // ─── getWhyThisMeal ───────────────────────────────────────────────────────

    @Test
    fun `getWhyThisMeal cuando API responde OK retorna Success`() = runTest {
        coEvery { apiService.getWhyThisMeal(any()) } returns ApiResponse(
            success = true, data = fakeWhyThisResponseDto(referenceType = "MEAL"),
        )

        val result = repository.getWhyThisMeal("meal-1")

        assertTrue(result is Result.Success)
    }

    @Test
    fun `getWhyThisMeal cuando API falla retorna Error`() = runTest {
        coEvery { apiService.getWhyThisMeal(any()) } throws Exception("Network error")

        val result = repository.getWhyThisMeal("meal-1")

        assertTrue(result is Result.Error)
    }

    // ─── getGlossaryTerm ──────────────────────────────────────────────────────

    @Test
    fun `getGlossaryTerm cuando API responde OK retorna Success`() = runTest {
        coEvery { apiService.getGlossaryTerm(any()) } returns ApiResponse(
            success = true, data = fakeGlossaryDefinitionResponseDto(),
        )

        val result = repository.getGlossaryTerm("Hypertrophy")

        assertTrue(result is Result.Success)
        assertEquals("Hypertrophy", (result as Result.Success).data.term)
    }

    @Test
    fun `getGlossaryTerm cuando API falla retorna Error`() = runTest {
        coEvery { apiService.getGlossaryTerm(any()) } throws Exception("Network error")

        val result = repository.getGlossaryTerm("Hypertrophy")

        assertTrue(result is Result.Error)
    }

    // ─── getHistory ───────────────────────────────────────────────────────────

    @Test
    fun `getHistory cuando API responde OK retorna Success con lista`() = runTest {
        coEvery { apiService.getHistory() } returns ApiResponse(
            success = true, data = listOf(fakeContextualExplanationResponseDto()),
        )

        val result = repository.getHistory()

        assertTrue(result is Result.Success)
        assertEquals(1, (result as Result.Success).data.size)
    }

    @Test
    fun `getHistory cuando API falla retorna Error`() = runTest {
        coEvery { apiService.getHistory() } throws Exception("Server error")

        val result = repository.getHistory()

        assertTrue(result is Result.Error)
    }

    // ─── updateKnowledgeLevel ─────────────────────────────────────────────────

    @Test
    fun `updateKnowledgeLevel cuando API responde OK retorna Success`() = runTest {
        coEvery { apiService.updateKnowledgeLevel(any()) } returns ApiResponse(
            success = true, data = "INTERMEDIATE",
        )

        val result = repository.updateKnowledgeLevel("INTERMEDIATE")

        assertTrue(result is Result.Success)
        assertEquals("INTERMEDIATE", (result as Result.Success).data)
    }

    @Test
    fun `updateKnowledgeLevel cuando API falla retorna Error`() = runTest {
        coEvery { apiService.updateKnowledgeLevel(any()) } throws Exception("Network error")

        val result = repository.updateKnowledgeLevel("INTERMEDIATE")

        assertTrue(result is Result.Error)
    }
}

