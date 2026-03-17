package com.jlsh.aifit.feature.training.domain.model

import java.time.DayOfWeek

data class TrainingDay(
    val id: String,
    val dayNumber: Int,
    val name: String,
    val estimatedDurationMinutes: Int,
    val exercises: List<TrainingExercise>,
    val dayOfWeek: DayOfWeek? = null,
)

