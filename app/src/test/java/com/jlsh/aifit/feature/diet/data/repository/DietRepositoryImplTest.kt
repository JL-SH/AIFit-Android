package com.jlsh.aifit.feature.diet.data.repository

import app.cash.turbine.test
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.core.session.SessionManager
import com.jlsh.aifit.feature.diet.data.api.DietApiService
import com.jlsh.aifit.feature.diet.data.local.DietPlanDao
import com.jlsh.aifit.feature.diet.data.local.DietPlanDetailCacheDao
import com.jlsh.aifit.feature.diet.data.local.DietPlanDetailCacheEntity
import com.jlsh.aifit.feature.diet.domain.DietActivePlanNotifier
import com.jlsh.aifit.testutil.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

class DietRepositoryImplTest {

    private val apiService: DietApiService = mockk()
    private val dao: DietPlanDao = mockk(relaxUnitFun = true)
    private val detailCacheDao: DietPlanDetailCacheDao = mockk(relaxUnitFun = true)
    private val sessionManager: SessionManager = mockk()
    private val activePlanNotifier: DietActivePlanNotifier = mockk(relaxUnitFun = true)
    private lateinit var sut: DietRepositoryImpl

    @Before
    fun setUp() {
        coEvery { detailCacheDao.getById(any()) } returns null
        sut = DietRepositoryImpl(apiService, dao, detailCacheDao, sessionManager, activePlanNotifier)
    }

    // ─── getDietPlans ──────────────────────────────────────────────────────────

    @Test
    fun `getPlans emite Loading, cache y luego dato fresco de API`() = runTest {
        val cached = listOf(fakeDietPlanEntity(id = "cached-1"))
        val freshDto = listOf(fakeDietPlanSummaryResponseDto(id = "fresh-1"))

        every { sessionManager.getUserId() } returns FAKE_USER_ID
        coEvery { dao.getAllByUserId(FAKE_USER_ID) } returns cached
        coEvery { apiService.getDietPlans() } returns ApiResponse(success = true, data = freshDto)

        sut.getDietPlans().test {
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
        coEvery { apiService.getDietPlans() } throws IOException("timeout")

        sut.getDietPlans().test {
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            val error = awaitItem()
            assertTrue(error is Result.Error)

            awaitComplete()
        }
    }

    @Test
    fun `getPlans emite solo cache cuando API falla pero hay cache`() = runTest {
        val cached = listOf(fakeDietPlanEntity())

        every { sessionManager.getUserId() } returns FAKE_USER_ID
        coEvery { dao.getAllByUserId(FAKE_USER_ID) } returns cached
        coEvery { apiService.getDietPlans() } throws IOException("timeout")

        sut.getDietPlans().test {
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            val cacheResult = awaitItem()
            assertTrue(cacheResult is Result.Success)
            assertEquals(1, (cacheResult as Result.Success).data.size)

            awaitComplete()
        }
    }

    @Test
    fun `getPlans emite Error cuando userId es null`() = runTest {
        every { sessionManager.getUserId() } returns null

        sut.getDietPlans().test {
            val loading = awaitItem()
            assertTrue(loading is Result.Loading)

            val error = awaitItem()
            assertTrue(error is Result.Error)

            awaitComplete()
        }
    }

    // ─── getDietPlanDetail ─────────────────────────────────────────────────────

    @Test
    fun `getPlanDetail retorna plan con days cuando API tiene éxito`() = runTest {
        val dto = fakeDietPlanResponseDto(id = "dp-1")
        every { sessionManager.getUserId() } returns FAKE_USER_ID
        coEvery { apiService.getDietPlanById("dp-1") } returns
            ApiResponse(success = true, data = dto)

        val result = sut.getDietPlanDetail("dp-1")

        assertTrue(result is Result.Success)
        val plan = (result as Result.Success).data
        assertEquals("dp-1", plan.id)
        assertEquals(1, plan.days.size)
    }

    @Test
    fun `getPlanDetail retorna Error cuando API falla`() = runTest {
        coEvery { apiService.getDietPlanById(any()) } throws IOException("timeout")

        val result = sut.getDietPlanDetail("dp-1")

        assertTrue(result is Result.Error)
    }

    // ─── generateDietPlan ──────────────────────────────────────────────────────

    @Test
    fun `generatePlan retorna plan y lo guarda en cache`() = runTest {
        val dto = fakeDietPlanResponseDto(id = "gen-1")
        every { sessionManager.getUserId() } returns FAKE_USER_ID
        coEvery { apiService.generateDietPlan(any()) } returns
            ApiResponse(success = true, data = dto)

        val result = sut.generateDietPlan(fakeGenerateDietPlanRequestDto())

        assertTrue(result is Result.Success)
        assertEquals("gen-1", (result as Result.Success).data.id)
        coVerify { dao.upsertAll(any()) }
    }

    @Test
    fun `generatePlan retorna Error cuando userId es null`() = runTest {
        every { sessionManager.getUserId() } returns null

        val result = sut.generateDietPlan(fakeGenerateDietPlanRequestDto())

        assertTrue(result is Result.Error)
    }

    @Test
    fun `generatePlan retorna Error cuando API falla`() = runTest {
        every { sessionManager.getUserId() } returns FAKE_USER_ID
        coEvery { apiService.generateDietPlan(any()) } throws IOException("fail")

        val result = sut.generateDietPlan(fakeGenerateDietPlanRequestDto())

        assertTrue(result is Result.Error)
    }

    // ─── deleteDietPlan ────────────────────────────────────────────────────────

    @Test
    fun `deletePlan retorna Success y borra de Room inmediatamente`() = runTest {
        coEvery { dao.getById("dp-1") } returns fakeDietPlanEntity(id = "dp-1")
        coEvery { apiService.deleteDietPlan("dp-1") } returns
            ApiResponse(success = true, data = Unit)

        val result = sut.deleteDietPlan("dp-1")

        assertTrue(result is Result.Success)
        coVerify { dao.deleteById("dp-1") }
    }

    @Test
    fun `deletePlan restaura snapshot en Room cuando API falla`() = runTest {
        val backup = fakeDietPlanEntity(id = "dp-1")
        coEvery { dao.getById("dp-1") } returns backup
        coEvery { apiService.deleteDietPlan(any()) } throws IOException("fail")

        val result = sut.deleteDietPlan("dp-1")

        assertTrue(result is Result.Error)
        coVerify { dao.deleteById("dp-1") }
        coVerify { dao.upsertAll(listOf(backup)) }
    }

    // ─── setActiveDietPlan ─────────────────────────────────────────────────────

    @Test
    fun `setActiveDietPlan notifica y guarda detail cache cuando respuesta tiene days`() = runTest {
        val dto = fakeDietPlanResponseDto(id = "dp-2", status = "ACTIVE")
        every { sessionManager.getUserId() } returns FAKE_USER_ID
        coEvery { dao.getAllByUserId(FAKE_USER_ID) } returns emptyList()
        coEvery { apiService.activateDietPlan("dp-2") } returns ApiResponse(success = true, data = dto)

        val result = sut.setActiveDietPlan("dp-2")

        assertTrue(result is Result.Success)
        verify { activePlanNotifier.notifyActivePlanChanged("dp-2") }
        coVerify {
            detailCacheDao.upsert(
                match<DietPlanDetailCacheEntity> { it.planId == "dp-2" && it.detailJson.isNotBlank() },
            )
        }
    }

    @Test
    fun `getCachedDietPlans lee solo Room sin llamar API`() = runTest {
        val cached = listOf(fakeDietPlanEntity(id = "cached-1"))
        every { sessionManager.getUserId() } returns FAKE_USER_ID
        coEvery { dao.getAllByUserId(FAKE_USER_ID) } returns cached

        val result = sut.getCachedDietPlans()

        assertEquals(1, result.size)
        assertEquals("cached-1", result[0].id)
        coVerify(exactly = 0) { apiService.getDietPlans() }
    }
}


