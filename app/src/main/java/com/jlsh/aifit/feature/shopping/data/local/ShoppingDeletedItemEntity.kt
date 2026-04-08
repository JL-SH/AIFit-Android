package com.jlsh.aifit.feature.shopping.data.local

import androidx.room.Entity

@Entity(
    tableName = "shopping_deleted_items",
    primaryKeys = ["shoppingListId", "itemName", "category"],
)
data class ShoppingDeletedItemEntity(
    val shoppingListId: String,
    val itemName: String,
    val category: String,
)

