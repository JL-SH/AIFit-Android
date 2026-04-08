package com.jlsh.aifit.feature.shopping.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_local_items")
data class ShoppingLocalItemEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val shoppingListId: String,
    val itemName: String,
    val category: String,
    val totalQuantity: Double,
    val unit: String,
    val notes: String?,
)

