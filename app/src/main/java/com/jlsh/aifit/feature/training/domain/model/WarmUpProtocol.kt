package com.jlsh.aifit.feature.training.domain.model

data class WarmUpProtocol(
    val trainingDayId: String,
    val estimatedTotalLoad: Double,
    val exercises: List<WarmUpExercise>,
)

