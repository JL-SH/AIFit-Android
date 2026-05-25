package com.jlsh.aifit.feature.shopping.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ShoppingListResponseDto(
    val id: String,
    val dietPlanId: String? = null,
    val period: String,
    val categories: List<ShoppingCategoryGroupResponseDto>? = null,
    val generatedAt: String? = null,
)

