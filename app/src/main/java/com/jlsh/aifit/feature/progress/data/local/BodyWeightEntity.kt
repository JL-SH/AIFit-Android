package com.jlsh.aifit.feature.progress.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_weight_logs")
data class BodyWeightEntity(
    @PrimaryKey val id: String,
    val weight: Double,
    val date: Long,
    val notes: String?,
    val createdAt: Long,
)

