package com.jlsh.aifit.feature.nutrition.data.repository

import app.cash.turbine.test
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.nutrition.data.api.NutritionLogApiService
import com.jlsh.aifit.feature.nutrition.data.local.NutritionLogDao
import com.jlsh.aifit.testutil.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class NutritionLogRepositoryImplTest {

    private val apiService: NutritionLogApiService = mockk()
    private val dao: NutritionLogDao = mockk()
    private lateinit var sut: NutritionLogRepositoryImpl

    @Before
    fun setUp() {
        sut = NutritionLogRepositoryImpl(apiService, dao)
    }

    // ─── getNutritionLog ───────────────────────────────────────────────────────

    @Test
    fun `getNutritionLog emite Loading, cache y luego dato fresco de API`() = runTest {
        val cachedEntity = fakeNutritionLogEntity()
        val freshDto = fakeNutritionLogResponseDto()
        coEvery { dao.getByDate(any()) } returns cachedEntity
        coEvery { apiService.getNutritionLog(any()) } returns ApiResponse(
            success = true, data = freshDto,
        )
        coEvery { dao.upsert(any()) } just Runs

        sut.getNutritionLog(LocalDate.of(2026, 4, 6)).test {
            // Primera emisión: Loading
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            // Segunda emisión: cache
            val cached = awaitItem()
            assertTrue(cached is Result.Success)
            assertEquals("nutrition-log-1", (cached as Result.Success).data.id)

            // Tercera emisión: dato fresco
            val fresh = awaitItem()
            assertTrue(fresh is Result.Success)
            assertEquals("nutrition-log-1", (fresh as Result.Success).data.id)

            coVerify { dao.upsert(any()) }
            awaitComplete()
        }
    }

    @Test
    fun `getNutritionLog emite Error cuando API falla y no hay cache`() = runTest {
        coEvery { dao.getByDate(any()) } returns null
        coEvery { apiService.getNutritionLog(any()) } throws java.io.IOException("timeout")

        sut.getNutritionLog(LocalDate.of(2026, 4, 6)).test {
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            val error = awaitItem()
            assertTrue(error is Result.Error)
            awaitComplete()
        }
    }

    @Test
    fun `getNutritionLog no emite Error cuando API falla pero hay cache`() = runTest {
        val cachedEntity = fakeNutritionLogEntity()
        coEvery { dao.getByDate(any()) } returns cachedEntity
        coEvery { apiService.getNutritionLog(any()) } throws java.io.IOException("timeout")

        sut.getNutritionLog(LocalDate.of(2026, 4, 6)).test {
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            val cached = awaitItem()
            assertTrue(cached is Result.Success)

            // No debe emitir Error porque hay cache
            awaitComplete()
        }
    }

    // ─── getNutritionHistory ───────────────────────────────────────────────────

    @Test
    fun `getNutritionHistory retorna Success con lista de logs`() = runTest {
        coEvery { apiService.getNutritionHistory(any(), any()) } returns ApiResponse(
            success = true, data = listOf(fakeNutritionLogResponseDto()),
        )

        val result = sut.getNutritionHistory("2026-04-01", "2026-04-06")

        assertTrue(result is Result.Success)
        assertEquals(1, (result as Result.Success).data.size)
    }

    @Test
    fun `getNutritionHistory retorna Error cuando API falla`() = runTest {
        coEvery { apiService.getNutritionHistory(any(), any()) } throws java.io.IOException("fail")

        val result = sut.getNutritionHistory("2026-04-01", "2026-04-06")

        assertTrue(result is Result.Error)
    }

    // ─── trackMeal ─────────────────────────────────────────────────────────────

    @Test
    fun `trackMeal retorna Success e invalida cache del día`() = runTest {
        val request = fakeTrackMealRequestDto()
        coEvery { apiService.trackMeal(request) } returns ApiResponse(
            success = true, data = fakeMealLogResponseDto(),
        )
        coEvery { dao.deleteByDate(any()) } just Runs

        val result = sut.trackMeal(request)

        assertTrue(result is Result.Success)
        coVerify { dao.deleteByDate(any()) }
    }

    @Test
    fun `trackMeal retorna Error cuando API falla`() = runTest {
        val request = fakeTrackMealRequestDto()
        coEvery { apiService.trackMeal(request) } throws java.io.IOException("fail")

        val result = sut.trackMeal(request)

        assertTrue(result is Result.Error)
    }

    // ─── analyzeMealFromText ───────────────────────────────────────────────────

    @Test
    fun `analyzeMealFromText retorna Success e invalida cache del día`() = runTest {
        val request = fakeAnalyzeMealFromTextRequestDto()
        coEvery { apiService.analyzeMealFromText(request) } returns ApiResponse(
            success = true, data = fakeMealLogResponseDto(),
        )
        coEvery { dao.deleteByDate(any()) } just Runs

        val result = sut.analyzeMealFromText(request)

        assertTrue(result is Result.Success)
        coVerify { dao.deleteByDate(any()) }
    }

    @Test
    fun `analyzeMealFromText retorna Error cuando API falla`() = runTest {
        val request = fakeAnalyzeMealFromTextRequestDto()
        coEvery { apiService.analyzeMealFromText(request) } throws java.io.IOException("fail")

        val result = sut.analyzeMealFromText(request)

        assertTrue(result is Result.Error)
    }

    // ─── deleteMealLog ─────────────────────────────────────────────────────────

    @Test
    fun `deleteMealLog retorna Success e invalida cache`() = runTest {
        coEvery { apiService.deleteMealLog(any()) } returns ApiResponse(
            success = true, data = Unit,
        )
        coEvery { dao.deleteByDate(any()) } just Runs

        val result = sut.deleteMealLog("meal-1")

        assertTrue(result is Result.Success)
        coVerify { dao.deleteByDate(any()) }
    }

    @Test
    fun `deleteMealLog retorna Error cuando API falla`() = runTest {
        coEvery { apiService.deleteMealLog(any()) } throws java.io.IOException("fail")

        val result = sut.deleteMealLog("meal-1")

        assertTrue(result is Result.Error)
    }
}


