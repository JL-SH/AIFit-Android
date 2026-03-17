package com.jlsh.aifit.feature.workout.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class JointPainEntryDto(
    val zone: String,
    val note: String? = null,
)

