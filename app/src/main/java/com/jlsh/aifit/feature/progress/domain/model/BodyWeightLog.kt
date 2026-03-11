package com.jlsh.aifit.feature.progress.domain.model

import java.time.LocalDate

data class BodyWeightLog(
    val id: String,
    val weight: Double,
    val date: LocalDate,
    val notes: String?,
    val createdAt: LocalDate,
)

