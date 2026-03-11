package com.jlsh.aifit.feature.shopping.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {

    @Query("SELECT * FROM shopping_lists ORDER BY generatedAt DESC")
    suspend fun getAllLists(): List<ShoppingListEntity>

    @Upsert
    suspend fun upsertList(entity: ShoppingListEntity)

    @Query("DELETE FROM shopping_lists WHERE id = :id")
    suspend fun deleteList(id: String)

    @Query("SELECT * FROM shopping_item_checks WHERE shoppingListId = :shoppingListId")
    fun getChecks(shoppingListId: String): Flow<List<ShoppingItemCheckEntity>>

    @Upsert
    suspend fun upsertCheck(entity: ShoppingItemCheckEntity)
}

