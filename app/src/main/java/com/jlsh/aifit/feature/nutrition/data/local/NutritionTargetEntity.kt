package com.jlsh.aifit.feature.nutrition.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nutrition_targets")
data class NutritionTargetEntity(
    @PrimaryKey val id: String,
    val calorieTarget: Int,
    val proteinTarget: Double,
    val carbsTarget: Double,
    val fatTarget: Double,
    val effectiveFrom: Long,
    val setBy: String,
)
