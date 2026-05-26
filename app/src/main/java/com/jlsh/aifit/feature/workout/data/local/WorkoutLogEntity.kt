package com.jlsh.aifit.feature.workout.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_logs",
    indices = [Index(value = ["date"])],
)
data class WorkoutLogEntity(
    @PrimaryKey val id: String,
    val trainingPlanId: String,
    val trainingDayId: String,
    val date: Long,
    val durationMinutes: Int?,
    val perceivedExertion: Int?,
    val totalExercises: Int,
    val completedAt: Long,
    val isLocked: Boolean = false,
)

