package com.jlsh.aifit.feature.shopping.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
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

    // ── Local item edits ──────────────────────────────────────────────────────

    @Query("SELECT * FROM shopping_local_items WHERE shoppingListId = :shoppingListId")
    fun getLocalItems(shoppingListId: String): Flow<List<ShoppingLocalItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocalItem(entity: ShoppingLocalItemEntity)

    @Query("DELETE FROM shopping_local_items WHERE localId = :localId")
    suspend fun deleteLocalItem(localId: Long)

    @Query("DELETE FROM shopping_local_items WHERE shoppingListId = :shoppingListId")
    suspend fun deleteAllLocalItems(shoppingListId: String)

    @Query("SELECT * FROM shopping_deleted_items WHERE shoppingListId = :shoppingListId")
    fun getDeletedItems(shoppingListId: String): Flow<List<ShoppingDeletedItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeletedItem(entity: ShoppingDeletedItemEntity)

    @Query("DELETE FROM shopping_deleted_items WHERE shoppingListId = :shoppingListId AND itemName = :itemName AND category = :category")
    suspend fun removeDeletedItem(shoppingListId: String, itemName: String, category: String)

    @Query("DELETE FROM shopping_deleted_items WHERE shoppingListId = :shoppingListId")
    suspend fun clearDeletedItems(shoppingListId: String)
}

