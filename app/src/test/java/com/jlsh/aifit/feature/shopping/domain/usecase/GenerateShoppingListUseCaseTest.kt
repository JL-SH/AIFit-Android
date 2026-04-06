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

class GenerateShoppingListUseCaseTest {

    private val repository: ShoppingRepository = mockk()
    private val useCase = GenerateShoppingListUseCase(repository)

    @Test
    fun `invoke retorna Success con lista generada`() = runTest {
        val list = fakeShoppingList()
        val request = fakeGenerateShoppingListRequestDto()
        coEvery { repository.generateList(request) } returns Result.Success(list)

        val result = useCase(request)

        assertTrue(result is Result.Success)
        assertEquals("slist-1", (result as Result.Success).data.id)
    }

    @Test
    fun `invoke retorna Error cuando repository falla`() = runTest {
        val request = fakeGenerateShoppingListRequestDto()
        coEvery { repository.generateList(request) } returns Result.Error(AppException.ServerException)

        val result = useCase(request)

        assertTrue(result is Result.Error)
    }
}

