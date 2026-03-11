package com.jlsh.aifit.feature.nutrition.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AnalyzeMealFromTextRequestDto(
    val date: String,
    val mealType: String,
    val time: String,
    val text: String,
)

