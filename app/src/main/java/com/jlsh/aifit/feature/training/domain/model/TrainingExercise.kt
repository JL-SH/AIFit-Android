package com.jlsh.aifit.feature.training.domain.model

data class TrainingExercise(
    val id: String,
    val name: String,
    val description: String?,
    val primaryMuscle: MuscleGroup,
    val secondaryMuscle: MuscleGroup?,
    val sets: Int,
    val repsMin: Int,
    val repsMax: Int,
    val restSeconds: Int,
    val notes: String?,
    val order: Int,
    val targetRpe: Int? = null,
)

