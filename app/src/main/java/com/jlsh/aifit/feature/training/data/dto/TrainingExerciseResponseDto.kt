package com.jlsh.aifit.feature.training.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TrainingExerciseResponseDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val primaryMuscle: String,
    val secondaryMuscle: String? = null,
    val sets: Int,
    val repsMin: Int,
    val repsMax: Int,
    val restSeconds: Int,
    val notes: String? = null,
    val order: Int,
    val targetRpe: Int? = null,
)

