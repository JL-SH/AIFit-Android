package com.jlsh.aifit.feature.shopping.domain.usecase

import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.shopping.domain.repository.ShoppingRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class DeleteShoppingListUseCaseTest {

    private val repository: ShoppingRepository = mockk()
    private val useCase = DeleteShoppingListUseCase(repository)

    @Test
    fun `invoke retorna Success cuando repository elimina correctamente`() = runTest {
        coEvery { repository.deleteList("slist-1") } returns Result.Success(Unit)

        val result = useCase("slist-1")

        assertTrue(result is Result.Success)
    }

    @Test
    fun `invoke retorna Error cuando repository falla`() = runTest {
        coEvery { repository.deleteList("slist-1") } returns Result.Error(AppException.ServerException)

        val result = useCase("slist-1")

        assertTrue(result is Result.Error)
    }
}

