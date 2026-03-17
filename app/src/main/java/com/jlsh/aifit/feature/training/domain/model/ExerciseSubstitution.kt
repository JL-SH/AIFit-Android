package com.jlsh.aifit.feature.training.domain.model

data class ExerciseSubstitution(
    val name: String,
    val primaryMuscle: MuscleGroup,
    val movementPattern: String,
    val description: String,
)

