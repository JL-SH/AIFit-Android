package com.jlsh.aifit.feature.shopping.domain.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.shopping.data.dto.GenerateShoppingListRequestDto
import com.jlsh.aifit.feature.shopping.data.local.ShoppingLocalItemEntity
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingList
import kotlinx.coroutines.flow.Flow

interface ShoppingRepository {
    fun getLists(): Flow<Result<List<ShoppingList>>>
    suspend fun getList(id: String): Result<ShoppingList>
    suspend fun generateList(request: GenerateShoppingListRequestDto): Result<ShoppingList>
    suspend fun deleteList(id: String): Result<Unit>
    fun getCheckStates(listId: String): Flow<Map<String, Boolean>>
    suspend fun toggleCheck(listId: String, itemName: String, category: String, checked: Boolean)

    // ── Local item edits ──────────────────────────────────────────────────────
    fun getLocalItems(listId: String): Flow<List<ShoppingLocalItemEntity>>
    suspend fun addLocalItem(listId: String, name: String, category: String, quantity: Double, unit: String, notes: String?)
    suspend fun deleteLocalItem(localId: Long)
    fun getDeletedItemKeys(listId: String): Flow<Set<String>>
    suspend fun markItemDeleted(listId: String, itemName: String, category: String)
    suspend fun unmarkItemDeleted(listId: String, itemName: String, category: String)
}

