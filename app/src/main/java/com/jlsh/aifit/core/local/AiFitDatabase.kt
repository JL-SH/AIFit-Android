package com.jlsh.aifit.core.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jlsh.aifit.feature.diet.data.local.DietPlanDao
import com.jlsh.aifit.feature.diet.data.local.DietPlanEntity
import com.jlsh.aifit.feature.training.data.local.TrainingPlanDao
import com.jlsh.aifit.feature.training.data.local.TrainingPlanEntity
import com.jlsh.aifit.feature.user.data.local.UserProfileDao
import com.jlsh.aifit.feature.user.data.local.UserProfileEntity
import com.jlsh.aifit.feature.workout.data.local.WorkoutLogDao
import com.jlsh.aifit.feature.workout.data.local.WorkoutLogEntity

@Database(
    entities = [
        UserProfileEntity::class,
        TrainingPlanEntity::class,
        DietPlanEntity::class,
        WorkoutLogEntity::class,
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AiFitDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun trainingPlanDao(): TrainingPlanDao
    abstract fun dietPlanDao(): DietPlanDao
    abstract fun workoutLogDao(): WorkoutLogDao
}
