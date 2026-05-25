package com.jlsh.aifit.feature.home.ui.state

import com.jlsh.aifit.feature.gamification.domain.model.AchievementDefinition
import com.jlsh.aifit.feature.gamification.domain.model.Streak
import com.jlsh.aifit.feature.gamification.domain.model.UserAchievement
import com.jlsh.aifit.feature.progress.domain.model.BodyWeightLog
import com.jlsh.aifit.feature.progress.domain.model.WeeklyProgressSummary

/**
 * Home screen status (daily dashboard).
 */
sealed class HomeUiState {

    /** Initial loading of the dashboard; skeleton until full snapshot.*/
    data object Loading : HomeUiState()

    /**
     * Unrecoverable error on upload (e.g. profile not available).
     *
     * @property message Error message to display to the user.
     */
    data class Error(val message: String) : HomeUiState()

    /**
     * Dashboard data ready to display.
     *
     * @property userName Name of the user for the greeting.
     * @property avatarUrl Profile photo URL, or null if there is no image.
     * @property activePlan Summary of the active training plan; null if there is no active plan.
     * @property todayTraining Today's training; null on a day of rest or without a plan.
     * @property todayNutrition Macros and calories consumed vs goal for the day.
     * @property nextMeal Next meal of the active diet plan.
     * @property streaks Gamification streaks (training, nutrition, etc.).
     * @property weeklySummary Weekly Adherence Summary; null until loaded in the background.
     * @property weightEntries Latest weight entries (up to 7 recent entries).
     * @property lastAchievement Last achievement unlocked in the last 7 days.
     * @property nextAchievement Next achievement to unlock.
     * @property trainingStreakDays Consecutive training streak days.
     * @property isTrainingHydrating There is an active plan but the detail is not yet cached (CTAs disabled).
     */
    data class Success(
        val userName: String,
        val avatarUrl: String?,
        val activePlan: ActivePlanSummary? = null,
        val todayTraining: TodayTrainingState?,
        val todayNutrition: TodayNutritionState?,
        val nextMeal: NextMealState,
        val streaks: List<Streak>,
        val weeklySummary: WeeklyProgressSummary?,
        val weightEntries: List<BodyWeightLog>,
        val lastAchievement: UserAchievement? = null,
        val nextAchievement: AchievementDefinition? = null,
        val trainingStreakDays: Int = 0,
        val isTrainingHydrating: Boolean = false,
    ) : HomeUiState()
}

/**
 * Light summary of the active training plan in [HomeUiState.Success].
 *
 * @property id Plan identifier.
 * @property name Display name of the plan.
 */
data class ActivePlanSummary(
    val id: String,
    val name: String,
)

/**
 * Training status scheduled for the current day.
 *
 * @property planId Identifier of the active plan.
 * @property dayId Identifier of today's training day.
 * @property planName Name of the plan.
 * @property dayName Name of the day (e.g. "Day 1 — Chest").
 * @property exerciseCount Total number of exercises for the day.
 * @property exerciseNames Names of exercises to display on the card (cropped).
 * @property adherencePercentage Percentage of weekly adherence to the plan (0–100).
 * @property isCompleted True if the user has already completed and locked today's workout.
 */
data class TodayTrainingState(
    val planId: String,
    val dayId: String,
    val planName: String,
    val dayName: String,
    val exerciseCount: Int,
    val exerciseNames: List<String>,
    val adherencePercentage: Float,
    val isCompleted: Boolean,
)

/**
 * Nutritional consumption of the day compared to the set objectives.
 *
 * @property caloriesConsumed Calories recorded today.
 * @property calorieTarget Daily calorie goal.
 * @property proteinConsumed Protein consumed (g).
 * @property proteinTarget Protein target (g).
 * @property carbsConsumed Carbohydrates consumed (g).
 * @property carbsTarget Carb target (g).
 * @property fatConsumed Fat consumed (g).
 * @property fatTarget Fat target (g).
 */
data class TodayNutritionState(
    val caloriesConsumed: Int,
    val calorieTarget: Int,
    val proteinConsumed: Double,
    val proteinTarget: Double,
    val carbsConsumed: Double,
    val carbsTarget: Double,
    val fatConsumed: Double,
    val fatTarget: Double,
)

/**
 * Status of the next meal according to the active diet plan.
 */
sealed class NextMealState {

    /** There is no active diet plan or the plan does not have days set up.*/
    data object NoPlan : NextMealState()

    /** All meals of the day have already passed according to the current time.*/
    data object AllDone : NextMealState()

    /**
     * There is a meal due later in the day.
     *
     * @property mealName Name of the meal.
     * @property estimatedTime Estimated time (plan text or inferred by type).
     * @property calories Calories in the food.
     * @property proteinG Protein in grams.
     * @property carbsG Carbohydrates in grams.
     * @property fatG Fat in grams.
     */
    data class Upcoming(
        val mealName: String,
        val estimatedTime: String,
        val calories: Int,
        val proteinG: Double,
        val carbsG: Double,
        val fatG: Double,
    ) : NextMealState()
}
