package com.jlsh.aifit.feature.shopping.domain.model

data class ShoppingItem(
    val name: String,
    val totalQuantity: Double,
    val unit: String,
    val notes: String?,
    val isChecked: Boolean = false,
)

