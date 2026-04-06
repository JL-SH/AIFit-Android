package com.jlsh.aifit.feature.shopping.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.shopping.domain.repository.ShoppingRepository
import com.jlsh.aifit.testutil.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GetShoppingListUseCaseTest {

    private val repository: ShoppingRepository = mockk()
    private val useCase = GetShoppingListUseCase(repository)

    @Test
    fun `invoke retorna Success con lista de compras`() = runTest {
        val list = fakeShoppingList()
        coEvery { repository.getList("slist-1") } returns Result.Success(list)

        val result = useCase("slist-1")

        assertTrue(result is Result.Success)
        assertEquals("slist-1", (result as Result.Success).data.id)
    }

    @Test
    fun `invoke retorna Error cuando repository falla`() = runTest {
        coEvery { repository.getList("slist-1") } returns Result.Error(AppException.ServerException)

        val result = useCase("slist-1")

        assertTrue(result is Result.Error)
    }
}

