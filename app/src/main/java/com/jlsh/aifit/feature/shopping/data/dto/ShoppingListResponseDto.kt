package com.jlsh.aifit.feature.shopping.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ShoppingListResponseDto(
    val id: String,
    val dietPlanId: String,
    val period: String,
    val categories: List<ShoppingCategoryGroupResponseDto> = emptyList(),
    val generatedAt: String,
)

