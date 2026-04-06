package com.jlsh.aifit.feature.training.data.repository

import android.util.Log
import app.cash.turbine.test
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.core.session.SessionManager
import com.jlsh.aifit.feature.training.data.api.TrainingApiService
import com.jlsh.aifit.feature.training.data.local.TrainingPlanDao
import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.testutil.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class TrainingRepositoryImplTest {

    private val apiService: TrainingApiService = mockk()
    private val dao: TrainingPlanDao = mockk(relaxUnitFun = true)
    private val sessionManager: SessionManager = mockk()
    private lateinit var sut: TrainingRepositoryImpl

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        sut = TrainingRepositoryImpl(apiService, dao, sessionManager)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    // ─── getTrainingPlans ──────────────────────────────────────────────────────

    @Test
    fun `getPlans emite Loading, cache y luego dato fresco de API`() = runTest {
        val cached = listOf(fakeTrainingPlanEntity(id = "cached-1"))
        val freshDto = listOf(fakeTrainingPlanSummaryResponseDto(id = "fresh-1"))

        every { sessionManager.getUserId() } returns FAKE_USER_ID
        coEvery { dao.getAllByUserId(FAKE_USER_ID) } returns cached
        coEvery { apiService.getTrainingPlans() } returns ApiResponse(success = true, data = freshDto)

        sut.getTrainingPlans().test {
            // 1ª emisión: Loading
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            // 2ª emisión: cache
            val cacheResult = awaitItem()
            assertTrue(cacheResult is Result.Success)
            assertEquals("cached-1", (cacheResult as Result.Success).data[0].id)

            // 3ª emisión: fresco de API
            val freshResult = awaitItem()
            assertTrue(freshResult is Result.Success)
            assertEquals("fresh-1", (freshResult as Result.Success).data[0].id)

            awaitComplete()
        }

        coVerify { dao.upsertAll(any()) }
    }

    @Test
    fun `getPlans emite Error cuando API falla y no hay cache`() = runTest {
        every { sessionManager.getUserId() } returns FAKE_USER_ID
        coEvery { dao.getAllByUserId(FAKE_USER_ID) } returns emptyList()
        coEvery { apiService.getTrainingPlans() } throws java.io.IOException("timeout")

        sut.getTrainingPlans().test {
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            val error = awaitItem()
            assertTrue(error is Result.Error)

            awaitComplete()
        }
    }

    @Test
    fun `getPlans emite solo cache cuando API falla pero hay cache`() = runTest {
        val cached = listOf(fakeTrainingPlanEntity())

        every { sessionManager.getUserId() } returns FAKE_USER_ID
        coEvery { dao.getAllByUserId(FAKE_USER_ID) } returns cached
        coEvery { apiService.getTrainingPlans() } throws java.io.IOException("timeout")

        sut.getTrainingPlans().test {
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            val cacheResult = awaitItem()
            assertTrue(cacheResult is Result.Success)
            assertEquals(1, (cacheResult as Result.Success).data.size)

            // No emite error porque ya emitió cache
            awaitComplete()
        }
    }

    @Test
    fun `getPlans emite Error cuando userId es null`() = runTest {
        every { sessionManager.getUserId() } returns null

        sut.getTrainingPlans().test {
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            val error = awaitItem()
            assertTrue(error is Result.Error)

            awaitComplete()
        }
    }

    // ─── getTrainingPlanDetail ──────────────────────────────────────────────────

    @Test
    fun `getPlanDetail retorna plan con days cuando API tiene éxito`() = runTest {
        val dto = fakeTrainingPlanResponseDto(id = "p-1")
        coEvery { apiService.getTrainingPlanById("p-1") } returns ApiResponse(success = true, data = dto)
        every { sessionManager.getUserId() } returns FAKE_USER_ID

        val result = sut.getTrainingPlanDetail("p-1")

        assertTrue(result is Result.Success)
        val plan = (result as Result.Success).data
        assertEquals("p-1", plan.id)
        assertEquals(1, plan.days.size)
        coVerify { dao.upsertAll(any()) }
    }

    @Test
    fun `getPlanDetail retorna Error cuando API falla`() = runTest {
        coEvery { apiService.getTrainingPlanById(any()) } throws java.io.IOException("timeout")

        val result = sut.getTrainingPlanDetail("p-1")

        assertTrue(result is Result.Error)
    }

    // ─── generateTrainingPlan ──────────────────────────────────────────────────

    @Test
    fun `generatePlan retorna plan y lo guarda en cache`() = runTest {
        val dto = fakeTrainingPlanResponseDto(id = "gen-1")
        every { sessionManager.getUserId() } returns FAKE_USER_ID
        coEvery { apiService.generateTrainingPlan(any()) } returns
            ApiResponse(success = true, data = dto)

        val result = sut.generateTrainingPlan(fakeGenerateTrainingPlanRequestDto())

        assertTrue(result is Result.Success)
        assertEquals("gen-1", (result as Result.Success).data.id)
        coVerify { dao.upsertAll(any()) }
    }

    @Test
    fun `generatePlan retorna Error cuando userId es null`() = runTest {
        every { sessionManager.getUserId() } returns null

        val result = sut.generateTrainingPlan(fakeGenerateTrainingPlanRequestDto())

        assertTrue(result is Result.Error)
    }

    @Test
    fun `generatePlan retorna Error cuando API falla`() = runTest {
        every { sessionManager.getUserId() } returns FAKE_USER_ID
        coEvery { apiService.generateTrainingPlan(any()) } throws java.io.IOException("fail")

        val result = sut.generateTrainingPlan(fakeGenerateTrainingPlanRequestDto())

        assertTrue(result is Result.Error)
    }

    // ─── deleteTrainingPlan ────────────────────────────────────────────────────

    @Test
    fun `deletePlan elimina de cache y retorna Success`() = runTest {
        val mockResponse = Response.success<Unit>(Unit)
        coEvery { apiService.deleteTrainingPlan("p-1") } returns mockResponse

        val result = sut.deleteTrainingPlan("p-1")

        assertTrue(result is Result.Success)
        coVerify { dao.deleteById("p-1") }
    }

    @Test
    fun `deletePlan retorna Error cuando API falla`() = runTest {
        val errorResponse = Response.error<Unit>(500, "error".toResponseBody(null))
        coEvery { apiService.deleteTrainingPlan(any()) } returns errorResponse

        val result = sut.deleteTrainingPlan("p-1")

        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { dao.deleteById(any()) }
    }

    // ─── activatePlan ──────────────────────────────────────────────────────────

    @Test
    fun `activatePlan retorna plan activado y actualiza cache`() = runTest {
        val dto = fakeTrainingPlanResponseDto(id = "p-1", status = "ACTIVE")
        every { sessionManager.getUserId() } returns FAKE_USER_ID
        coEvery { dao.getAllByUserId(FAKE_USER_ID) } returns emptyList()
        coEvery { apiService.activatePlan("p-1") } returns ApiResponse(success = true, data = dto)

        val result = sut.activatePlan("p-1")

        assertTrue(result is Result.Success)
        assertEquals(PlanStatus.ACTIVE, (result as Result.Success).data.status)
    }

    @Test
    fun `activatePlan retorna Error cuando userId es null`() = runTest {
        every { sessionManager.getUserId() } returns null

        val result = sut.activatePlan("p-1")

        assertTrue(result is Result.Error)
    }
}


