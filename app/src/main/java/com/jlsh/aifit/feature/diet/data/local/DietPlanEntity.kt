package com.jlsh.aifit.feature.diet.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diet_plans")
data class DietPlanEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val dailyCalories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val durationWeeks: Int,
    val preference: String,
    val status: String,
    val totalDays: Int,
    val createdAt: Long,
)

