package com.jlsh.aifit.feature.shopping.data.local

import androidx.room.Entity

@Entity(
    tableName = "shopping_item_checks",
    primaryKeys = ["shoppingListId", "itemName", "category"],
)
data class ShoppingItemCheckEntity(
    val shoppingListId: String,
    val itemName: String,
    val category: String,
    val isChecked: Boolean,
)

