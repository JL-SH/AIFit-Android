package com.jlsh.aifit.feature.shopping.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.shopping.data.dto.GenerateShoppingListRequestDto
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingList
import com.jlsh.aifit.feature.shopping.domain.repository.ShoppingRepository
import javax.inject.Inject

class GenerateShoppingListUseCase @Inject constructor(
    private val repository: ShoppingRepository,
) {
    suspend operator fun invoke(request: GenerateShoppingListRequestDto): Result<ShoppingList> =
        repository.generateList(request)
}

