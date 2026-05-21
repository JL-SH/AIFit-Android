package com.jlsh.aifit.feature.training.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_plan_details")
data class TrainingPlanDetailCacheEntity(
    @PrimaryKey val planId: String,
    val detailJson: String,
    val cachedAt: Long,
)
