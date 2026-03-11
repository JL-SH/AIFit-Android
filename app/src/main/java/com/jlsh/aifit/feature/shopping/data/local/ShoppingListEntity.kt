package com.jlsh.aifit.feature.shopping.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey val id: String,
    val dietPlanId: String,
    val period: String,
    val generatedAt: Long,
)

