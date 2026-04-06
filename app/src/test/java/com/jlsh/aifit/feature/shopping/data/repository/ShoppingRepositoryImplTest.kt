package com.jlsh.aifit.feature.shopping.data.repository

import app.cash.turbine.test
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.ApiResponse
import com.jlsh.aifit.feature.shopping.data.api.ShoppingApiService
import com.jlsh.aifit.feature.shopping.data.local.ShoppingDao
import com.jlsh.aifit.feature.shopping.data.local.ShoppingItemCheckEntity
import com.jlsh.aifit.testutil.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ShoppingRepositoryImplTest {

    private val apiService: ShoppingApiService = mockk()
    private val shoppingDao: ShoppingDao = mockk(relaxUnitFun = true)
    private lateinit var repository: ShoppingRepositoryImpl

    @Before
    fun setUp() {
        repository = ShoppingRepositoryImpl(apiService, shoppingDao)
    }

    // ── getLists ─────────────────────────────────────────────────────────────

    @Test
    fun `getLists emite Loading, cache y luego remote`() = runTest {
        val cached = listOf(fakeShoppingListEntity())
        val remote = listOf(fakeShoppingListResponseDto(id = "slist-remote"))

        coEvery { shoppingDao.getAllLists() } returns cached
        coEvery { apiService.getLists() } returns ApiResponse(success = true, data = remote)

        repository.getLists().test {
            assertTrue(awaitItem() is Result.Loading)

            val cacheResult = awaitItem()
            assertTrue(cacheResult is Result.Success)
            assertEquals("slist-1", (cacheResult as Result.Success).data[0].id)

            val remoteResult = awaitItem()
            assertTrue(remoteResult is Result.Success)
            assertEquals("slist-remote", (remoteResult as Result.Success).data[0].id)

            awaitComplete()
        }
    }

    @Test
    fun `getLists sin cache y API falla emite Loading y Error`() = runTest {
        coEvery { shoppingDao.getAllLists() } returns emptyList()
        coEvery { apiService.getLists() } throws RuntimeException("Fail")

        repository.getLists().test {
            assertTrue(awaitItem() is Result.Loading)
            assertTrue(awaitItem() is Result.Error)
            awaitComplete()
        }
    }

    @Test
    fun `getLists con cache y API falla emite Loading, cache y no error`() = runTest {
        coEvery { shoppingDao.getAllLists() } returns listOf(fakeShoppingListEntity())
        coEvery { apiService.getLists() } throws RuntimeException("Fail")

        repository.getLists().test {
            assertTrue(awaitItem() is Result.Loading)
            assertTrue(awaitItem() is Result.Success)
            awaitComplete()
        }
    }

    // ── getList ──────────────────────────────────────────────────────────────

    @Test
    fun `getList retorna Success y guarda en cache`() = runTest {
        val dto = fakeShoppingListResponseDto()
        coEvery { apiService.getList("slist-1") } returns ApiResponse(success = true, data = dto)

        val result = repository.getList("slist-1")

        assertTrue(result is Result.Success)
        coVerify { shoppingDao.upsertList(any()) }
    }

    @Test
    fun `getList retorna Error cuando API falla`() = runTest {
        coEvery { apiService.getList("slist-1") } throws RuntimeException("Fail")

        val result = repository.getList("slist-1")

        assertTrue(result is Result.Error)
    }

    // ── generateList ────────────────────────────────────────────────────────

    @Test
    fun `generateList retorna Success y guarda en cache`() = runTest {
        val dto = fakeShoppingListResponseDto()
        val request = fakeGenerateShoppingListRequestDto()
        coEvery { apiService.generateList(request) } returns ApiResponse(success = true, data = dto)

        val result = repository.generateList(request)

        assertTrue(result is Result.Success)
        coVerify { shoppingDao.upsertList(any()) }
    }

    @Test
    fun `generateList retorna Error cuando API falla`() = runTest {
        val request = fakeGenerateShoppingListRequestDto()
        coEvery { apiService.generateList(request) } throws RuntimeException("Fail")

        val result = repository.generateList(request)

        assertTrue(result is Result.Error)
    }

    // ── deleteList ──────────────────────────────────────────────────────────

    @Test
    fun `deleteList retorna Success y elimina de cache`() = runTest {
        coEvery { apiService.deleteList("slist-1") } returns ApiResponse(success = true, data = Unit)

        val result = repository.deleteList("slist-1")

        assertTrue(result is Result.Success)
        coVerify { shoppingDao.deleteList("slist-1") }
    }

    @Test
    fun `deleteList retorna Error cuando API falla`() = runTest {
        coEvery { apiService.deleteList("slist-1") } throws RuntimeException("Fail")

        val result = repository.deleteList("slist-1")

        assertTrue(result is Result.Error)
    }

    // ── getCheckStates ──────────────────────────────────────────────────────

    @Test
    fun `getCheckStates retorna mapa de checks desde Room`() = runTest {
        val checks = listOf(
            ShoppingItemCheckEntity("slist-1", "Chicken", "PROTEINS", true),
            ShoppingItemCheckEntity("slist-1", "Rice", "GRAINS_AND_CARBS", false),
        )
        every { shoppingDao.getChecks("slist-1") } returns flowOf(checks)

        repository.getCheckStates("slist-1").test {
            val map = awaitItem()
            assertEquals(true, map["PROTEINS:Chicken"])
            assertEquals(false, map["GRAINS_AND_CARBS:Rice"])
            awaitComplete()
        }
    }

    // ── toggleCheck ─────────────────────────────────────────────────────────

    @Test
    fun `toggleCheck llama upsertCheck en dao`() = runTest {
        repository.toggleCheck("slist-1", "Chicken", "PROTEINS", true)

        coVerify {
            shoppingDao.upsertCheck(
                ShoppingItemCheckEntity("slist-1", "Chicken", "PROTEINS", true)
            )
        }
    }
}

