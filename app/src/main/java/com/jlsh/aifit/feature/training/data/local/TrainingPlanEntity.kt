package com.jlsh.aifit.feature.training.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "training_plans",
    indices = [Index(value = ["userId"])],
)
data class TrainingPlanEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val description: String?,
    val status: String,
    val frequencyDaysPerWeek: Int,
    val durationWeeks: Int,
    val goalType: String,
    val fitnessLevel: String,
    val location: String,
    val totalDays: Int,
    val createdAt: Long,
)

