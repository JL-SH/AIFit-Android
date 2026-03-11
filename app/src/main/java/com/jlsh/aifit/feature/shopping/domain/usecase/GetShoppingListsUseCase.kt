package com.jlsh.aifit.feature.shopping.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingList
import com.jlsh.aifit.feature.shopping.domain.repository.ShoppingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetShoppingListsUseCase @Inject constructor(
    private val repository: ShoppingRepository,
) {
    operator fun invoke(): Flow<Result<List<ShoppingList>>> =
        repository.getLists()
}

