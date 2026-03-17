package com.jlsh.aifit.feature.training.domain.model

data class WarmUpExercise(
    val name: String,
    val description: String,
    val sets: Int,
    val reps: Int,
    val durationSeconds: Int?,
)

