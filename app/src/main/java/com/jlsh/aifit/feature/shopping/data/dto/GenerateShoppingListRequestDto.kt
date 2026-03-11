package com.jlsh.aifit.feature.shopping.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GenerateShoppingListRequestDto(
    val dietPlanId: String? = null,
    val period: String,
)

