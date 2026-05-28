package com.jlsh.aifit.feature.training.data.repository

import android.util.Log
import app.cash.turbine.test
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.core.session.SessionManager
import com.jlsh.aifit.feature.training.data.api.TrainingApiService
import com.jlsh.aifit.feature.training.data.local.TrainingPlanDao
import com.jlsh.aifit.feature.training.data.local.TrainingPlanDetailCacheDao
import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.testutil.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class TrainingRepositoryImplTest {

    private val apiService: TrainingApiService = mockk()
    private val dao: TrainingPlanDao = mockk(relaxUnitFun = true)
    private val detailCacheDao: TrainingPlanDetailCacheDao = mockk(relaxUnitFun = true)
    private val sessionManager: SessionManager = mockk()
    private lateinit var sut: TrainingRepositoryImpl

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        coEvery { detailCacheDao.getById(any()) } returns null

        sut = TrainingRepositoryImpl(apiService, dao, detailCacheDao, sessionManager)
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
            // 1st broadcast: Loading
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            // 2nd broadcast: cache
            val cacheResult = awaitItem()
            assertTrue(cacheResult is Result.Success)
            assertEquals("cached-1", (cacheResult as Result.Success).data[0].id)

            // 3rd issue: fresh from API
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

            // It does not issue an error because it has already issued cache
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
        coEvery { apiService.deleteTrainingPlan("p-1") } returns Response.success(Unit)
        coEvery { dao.getById("p-1") } returns null

        val result = sut.deleteTrainingPlan("p-1")

        assertTrue(result is Result.Success)
        coVerify { dao.deleteById("p-1") }
    }

    @Test
    fun `deletePlan retorna Error cuando API falla`() = runTest {
        val snapshot = fakeTrainingPlanEntity(id = "p-1")
        coEvery { dao.getById("p-1") } returns snapshot
        coEvery { apiService.deleteTrainingPlan(any()) } returns
            Response.error(
                500,
                "{\"message\":\"error\"}".toResponseBody("application/json".toMediaType()),
            )

        val result = sut.deleteTrainingPlan("p-1")

        assertTrue(result is Result.Error)
        coVerify(exactly = 1) { dao.deleteById("p-1") }
        coVerify { dao.upsertAll(match { it.any { e -> e.id == "p-1" } }) }
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

    // ─── Reconciliation scoped to userId ────────────────────────────────────────

    @Test
    fun `getPlans usa deleteAllNotInIds con userId cuando API devuelve planes`() = runTest {
        val freshDto = listOf(fakeTrainingPlanSummaryResponseDto(id = "p-1"))

        every { sessionManager.getUserId() } returns FAKE_USER_ID
        coEvery { dao.getAllByUserId(FAKE_USER_ID) } returns emptyList()
        coEvery { apiService.getTrainingPlans() } returns ApiResponse(success = true, data = freshDto)

        sut.getTrainingPlans().test {
            // Loading
            awaitItem()
            // Network result (no cache emission because cache is empty)
            awaitItem()
            awaitComplete()
        }

        coVerify { dao.deleteAllNotInIds(FAKE_USER_ID, listOf("p-1")) }
    }

    @Test
    fun `getPlans usa deleteAllByUserId cuando API devuelve lista vacia`() = runTest {
        val cached = listOf(fakeTrainingPlanEntity())

        every { sessionManager.getUserId() } returns FAKE_USER_ID
        coEvery { dao.getAllByUserId(FAKE_USER_ID) } returns cached
        coEvery { apiService.getTrainingPlans() } returns ApiResponse(success = true, data = emptyList())

        sut.getTrainingPlans().test {
            // Loading
            awaitItem()
            // Cache
            awaitItem()
            // Network empty
            awaitItem()
            awaitComplete()
        }

        coVerify { dao.deleteAllByUserId(FAKE_USER_ID) }
        coVerify(exactly = 0) { dao.deleteAll() }
    }

    @Test
    fun `getPlans no borra planes de otro usuario durante reconciliacion`() = runTest {
        val ownPlanDto = fakeTrainingPlanSummaryResponseDto(id = "p-own")

        every { sessionManager.getUserId() } returns FAKE_USER_ID
        // Only the current user's plans are returned by getAllByUserId
        coEvery { dao.getAllByUserId(FAKE_USER_ID) } returns listOf(
            fakeTrainingPlanEntity(id = "p-own", userId = FAKE_USER_ID),
        )
        coEvery { apiService.getTrainingPlans() } returns ApiResponse(success = true, data = listOf(ownPlanDto))

        sut.getTrainingPlans().test {
            awaitItem() // Loading
            awaitItem() // Cache
            // Network result may be collapsed by distinctUntilChanged if data equals cache
            // so we just drain remaining items
            cancelAndConsumeRemainingEvents()
        }

        // Must use userId-scoped delete, not the unscoped one
        coVerify { dao.deleteAllNotInIds(FAKE_USER_ID, listOf("p-own")) }
        // Must never call the nuclear unscoped deleteAll
        coVerify(exactly = 0) { dao.deleteAll() }
    }
}


