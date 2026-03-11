package com.jlsh.aifit.feature.shopping.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.shopping.domain.repository.ShoppingRepository
import javax.inject.Inject

class DeleteShoppingListUseCase @Inject constructor(
    private val repository: ShoppingRepository,
) {
    suspend operator fun invoke(id: String): Result<Unit> =
        repository.deleteList(id)
}

