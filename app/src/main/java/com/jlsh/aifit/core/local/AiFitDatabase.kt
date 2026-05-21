package com.jlsh.aifit.core.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jlsh.aifit.feature.chat.data.local.ChatDao
import com.jlsh.aifit.feature.chat.data.local.ChatMessageEntity
import com.jlsh.aifit.feature.chat.data.local.ChatSessionEntity
import com.jlsh.aifit.feature.diet.data.local.DietPlanDao
import com.jlsh.aifit.feature.diet.data.local.DietPlanEntity
import com.jlsh.aifit.feature.nutrition.data.local.NutritionLogDao
import com.jlsh.aifit.feature.nutrition.data.local.NutritionLogEntity
import com.jlsh.aifit.feature.nutrition.data.local.NutritionTargetDao
import com.jlsh.aifit.feature.nutrition.data.local.NutritionTargetEntity
import com.jlsh.aifit.feature.progress.data.local.BodyWeightDao
import com.jlsh.aifit.feature.progress.data.local.BodyWeightEntity
import com.jlsh.aifit.feature.shopping.data.local.ShoppingDao
import com.jlsh.aifit.feature.shopping.data.local.ShoppingDeletedItemEntity
import com.jlsh.aifit.feature.shopping.data.local.ShoppingItemCheckEntity
import com.jlsh.aifit.feature.shopping.data.local.ShoppingListEntity
import com.jlsh.aifit.feature.shopping.data.local.ShoppingLocalItemEntity
import com.jlsh.aifit.feature.training.data.local.TrainingPlanDao
import com.jlsh.aifit.feature.training.data.local.TrainingPlanDetailCacheDao
import com.jlsh.aifit.feature.training.data.local.TrainingPlanDetailCacheEntity
import com.jlsh.aifit.feature.training.data.local.TrainingPlanEntity
import com.jlsh.aifit.feature.user.data.local.UserProfileDao
import com.jlsh.aifit.feature.user.data.local.UserProfileEntity
import com.jlsh.aifit.feature.workout.data.local.WorkoutLogDao
import com.jlsh.aifit.feature.workout.data.local.WorkoutLogEntity

/**
 * Central Room database for the AIFit application.
 *
 * Aggregates all feature-specific DAOs and entity types into a single
 * [RoomDatabase] instance. The schema covers user profiles, training and
 * diet plans, workout and nutrition logs, body-weight entries, AI chat
 * history, and shopping list data.
 *
 * **Schema version**: 12. Schema export is disabled; migrations are handled
 * manually in the Hilt `DatabaseModule`.
 */
@Database(
    entities = [
        UserProfileEntity::class,
        TrainingPlanEntity::class,
        DietPlanEntity::class,
        WorkoutLogEntity::class,
        NutritionLogEntity::class,
        NutritionTargetEntity::class,
        TrainingPlanDetailCacheEntity::class,
        BodyWeightEntity::class,
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        ShoppingListEntity::class,
        ShoppingItemCheckEntity::class,
        ShoppingLocalItemEntity::class,
        ShoppingDeletedItemEntity::class,
    ],
    version = 12,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AiFitDatabase : RoomDatabase() {

    /** @return The DAO for [UserProfileEntity] read/write operations. */
    abstract fun userProfileDao(): UserProfileDao

    /** @return The DAO for [TrainingPlanEntity] read/write operations. */
    abstract fun trainingPlanDao(): TrainingPlanDao

    /** @return The DAO for [DietPlanEntity] read/write operations. */
    abstract fun dietPlanDao(): DietPlanDao

    /** @return The DAO for [WorkoutLogEntity] read/write operations. */
    abstract fun workoutLogDao(): WorkoutLogDao

    /** @return The DAO for [NutritionLogEntity] read/write operations. */
    abstract fun nutritionLogDao(): NutritionLogDao

    /** @return The DAO for [NutritionTargetEntity] read/write operations. */
    abstract fun nutritionTargetDao(): NutritionTargetDao

    /** @return The DAO for [TrainingPlanDetailCacheEntity] read/write operations. */
    abstract fun trainingPlanDetailCacheDao(): TrainingPlanDetailCacheDao

    /** @return The DAO for [BodyWeightEntity] read/write operations. */
    abstract fun bodyWeightDao(): BodyWeightDao

    /** @return The DAO for [ChatSessionEntity] and [ChatMessageEntity] operations. */
    abstract fun chatDao(): ChatDao

    /** @return The DAO for all shopping-list entities. */
    abstract fun shoppingDao(): ShoppingDao
}
