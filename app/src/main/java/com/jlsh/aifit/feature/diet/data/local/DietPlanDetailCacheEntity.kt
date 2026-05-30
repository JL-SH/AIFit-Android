package com.jlsh.aifit.feature.diet.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diet_plan_details")
data class DietPlanDetailCacheEntity(
    @PrimaryKey val planId: String,
    val detailJson: String,
    val cachedAt: Long,
)
