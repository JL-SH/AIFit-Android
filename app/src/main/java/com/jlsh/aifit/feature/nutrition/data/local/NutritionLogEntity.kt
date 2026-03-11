package com.jlsh.aifit.feature.nutrition.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nutrition_logs")
data class NutritionLogEntity(
    @PrimaryKey val id: String,
    val date: Long,
    val totalCalories: Int,
    val totalProteinGrams: Double,
    val totalCarbsGrams: Double,
    val totalFatGrams: Double,
)

