package com.jlsh.aifit.feature.shopping.domain.usecase

import app.cash.turbine.test
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.shopping.domain.repository.ShoppingRepository
import com.jlsh.aifit.testutil.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GetShoppingListsUseCaseTest {

    private val repository: ShoppingRepository = mockk()
    private val useCase = GetShoppingListsUseCase(repository)

    @Test
    fun `invoke retorna Flow con Success cuando repository emite listas`() = runTest {
        val lists = listOf(fakeShoppingList(), fakeShoppingList(id = "slist-2"))
        every { repository.getLists() } returns flowOf(Result.Success(lists))

        useCase().test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(2, (result as Result.Success).data.size)
            awaitComplete()
        }
    }

    @Test
    fun `invoke retorna Flow con Error cuando repository falla`() = runTest {
        every { repository.getLists() } returns flowOf(Result.Error(AppException.NetworkException))

        useCase().test {
            assertTrue(awaitItem() is Result.Error)
            awaitComplete()
        }
    }
}

