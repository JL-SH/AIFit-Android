package com.jlsh.aifit.feature.training.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseSubstitutionResponseDto(
    val name: String,
    val primaryMuscle: String,
    val movementPattern: String,
    val description: String,
)

