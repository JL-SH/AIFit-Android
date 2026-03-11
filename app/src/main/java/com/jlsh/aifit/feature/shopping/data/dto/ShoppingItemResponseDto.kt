package com.jlsh.aifit.feature.shopping.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ShoppingItemResponseDto(
    val name: String,
    val totalQuantity: Double,
    val unit: String,
    val notes: String? = null,
)

