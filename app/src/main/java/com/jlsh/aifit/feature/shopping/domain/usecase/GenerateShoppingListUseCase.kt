package com.jlsh.aifit.feature.shopping.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.shopping.data.dto.GenerateShoppingListRequestDto
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingList
import com.jlsh.aifit.feature.shopping.domain.repository.ShoppingRepository
import javax.inject.Inject

/**
 * Use case that generates a shopping list from a diet plan and a period.
 *
 * @param repository Shopping list repository.
 */
class GenerateShoppingListUseCase @Inject constructor(
    private val repository: ShoppingRepository,
) {
    /**
     * Requests the backend to generate a new shopping list.
     *
     * @param request Parameters with optional diet plan and time period.
     * @return [Result.Success] with the generated list, or [Result.Error] if the request fails.
     */
    suspend operator fun invoke(request: GenerateShoppingListRequestDto): Result<ShoppingList> =
        repository.generateList(request)
}

