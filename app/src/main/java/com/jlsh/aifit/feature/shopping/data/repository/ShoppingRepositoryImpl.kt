package com.jlsh.aifit.feature.shopping.data.repository

import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.network.BaseRemoteDataSource
import com.jlsh.aifit.feature.shopping.data.api.ShoppingApiService
import com.jlsh.aifit.feature.shopping.data.dto.GenerateShoppingListRequestDto
import com.jlsh.aifit.feature.shopping.data.local.ShoppingDao
import com.jlsh.aifit.feature.shopping.data.local.ShoppingDeletedItemEntity
import com.jlsh.aifit.feature.shopping.data.local.ShoppingItemCheckEntity
import com.jlsh.aifit.feature.shopping.data.local.ShoppingLocalItemEntity
import com.jlsh.aifit.feature.shopping.data.mapper.ShoppingMapper.toDomain
import com.jlsh.aifit.feature.shopping.data.mapper.ShoppingMapper.toEntity
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingList
import com.jlsh.aifit.feature.shopping.domain.repository.ShoppingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ShoppingRepositoryImpl @Inject constructor(
    private val apiService: ShoppingApiService,
    private val shoppingDao: ShoppingDao,
) : BaseRemoteDataSource(), ShoppingRepository {

    override fun getLists(): Flow<Result<List<ShoppingList>>> = flow {
        emit(Result.Loading)

        val cached = shoppingDao.getAllLists()
        if (cached.isNotEmpty()) {
            emit(Result.Success(cached.map { it.toDomain() }))
        }

        when (val remote = safeApiCall { apiService.getLists() }) {
            is Result.Success -> {
                remote.data.forEach { dto -> shoppingDao.upsertList(dto.toEntity()) }
                emit(Result.Success(remote.data.map { it.toDomain() }))
            }
            is Result.Error -> {
                if (cached.isEmpty()) emit(remote)
            }
            else -> Unit
        }
    }

    override suspend fun getList(id: String): Result<ShoppingList> =
        when (val r = safeApiCall { apiService.getList(id) }) {
            is Result.Success -> {
                shoppingDao.upsertList(r.data.toEntity())
                Result.Success(r.data.toDomain())
            }
            is Result.Error -> r
            else -> Result.Loading
        }

    override suspend fun generateList(request: GenerateShoppingListRequestDto): Result<ShoppingList> =
        when (val r = safeApiCall { apiService.generateList(request) }) {
            is Result.Success -> {
                shoppingDao.upsertList(r.data.toEntity())
                Result.Success(r.data.toDomain())
            }
            is Result.Error -> r
            else -> Result.Loading
        }

    override suspend fun deleteList(id: String): Result<Unit> =
        when (val r = safeApiCall { apiService.deleteList(id) }) {
            is Result.Success -> {
                shoppingDao.deleteList(id)
                shoppingDao.deleteAllLocalItems(id)
                shoppingDao.clearDeletedItems(id)
                Result.Success(Unit)
            }
            is Result.Error -> r
            else -> Result.Loading
        }

    override fun getCheckStates(listId: String): Flow<Map<String, Boolean>> =
        shoppingDao.getChecks(listId).map { checks ->
            checks.associate { "${it.category}:${it.itemName}" to it.isChecked }
        }

    override suspend fun toggleCheck(
        listId: String,
        itemName: String,
        category: String,
        checked: Boolean,
    ) {
        shoppingDao.upsertCheck(
            ShoppingItemCheckEntity(
                shoppingListId = listId,
                itemName = itemName,
                category = category,
                isChecked = checked,
            )
        )
    }

    // ── Local item edits ──────────────────────────────────────────────────────

    override fun getLocalItems(listId: String): Flow<List<ShoppingLocalItemEntity>> =
        shoppingDao.getLocalItems(listId)

    override suspend fun addLocalItem(
        listId: String,
        name: String,
        category: String,
        quantity: Double,
        unit: String,
        notes: String?,
    ) {
        shoppingDao.insertLocalItem(
            ShoppingLocalItemEntity(
                shoppingListId = listId,
                itemName = name,
                category = category,
                totalQuantity = quantity,
                unit = unit,
                notes = notes,
            )
        )
    }

    override suspend fun deleteLocalItem(localId: Long) {
        shoppingDao.deleteLocalItem(localId)
    }

    override fun getDeletedItemKeys(listId: String): Flow<Set<String>> =
        shoppingDao.getDeletedItems(listId).map { items ->
            items.map { "${it.category}:${it.itemName}" }.toSet()
        }

    override suspend fun markItemDeleted(listId: String, itemName: String, category: String) {
        shoppingDao.insertDeletedItem(
            ShoppingDeletedItemEntity(
                shoppingListId = listId,
                itemName = itemName,
                category = category,
            )
        )
    }

    override suspend fun unmarkItemDeleted(listId: String, itemName: String, category: String) {
        shoppingDao.removeDeletedItem(listId, itemName, category)
    }
}

