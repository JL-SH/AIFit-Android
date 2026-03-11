package com.jlsh.aifit.feature.shopping.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ShoppingCategoryGroupResponseDto(
    val category: String,
    val items: List<ShoppingItemResponseDto> = emptyList(),
)

