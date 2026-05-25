package com.jlsh.aifit.feature.shopping.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ShoppingCategoryGroupResponseDto(
    val category: String? = null,
    val items: List<ShoppingItemResponseDto>? = null,
)

