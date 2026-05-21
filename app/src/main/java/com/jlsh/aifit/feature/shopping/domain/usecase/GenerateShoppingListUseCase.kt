package com.jlsh.aifit.feature.shopping.domain.usecase

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.shopping.data.dto.GenerateShoppingListRequestDto
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingList
import com.jlsh.aifit.feature.shopping.domain.repository.ShoppingRepository
import javax.inject.Inject

/**
 * Caso de uso que genera una lista de la compra a partir de un plan de dieta y un período.
 *
 * @param repository Repositorio de listas de compra.
 */
class GenerateShoppingListUseCase @Inject constructor(
    private val repository: ShoppingRepository,
) {
    /**
     * Solicita al backend la generación de una nueva lista de la compra.
     *
     * @param request Parámetros con el plan de dieta opcional y el período temporal.
     * @return [Result.Success] con la lista generada, o [Result.Error] si falla la petición.
     */
    suspend operator fun invoke(request: GenerateShoppingListRequestDto): Result<ShoppingList> =
        repository.generateList(request)
}

