package com.jlsh.aifit.feature.shopping.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ShoppingItemResponseDto(
    val name: String? = null,
    val totalQuantity: Double? = null,
    val unit: String? = null,
    val notes: String? = null,
)

