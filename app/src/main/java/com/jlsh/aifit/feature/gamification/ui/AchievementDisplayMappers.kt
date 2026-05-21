package com.jlsh.aifit.feature.gamification.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.jlsh.aifit.R
import com.jlsh.aifit.feature.gamification.domain.model.AchievementDefinition

@StringRes
fun String.toAchievementNameRes(): Int? = when (this) {
    // Backend (AchievementSeederConfig)
    "FIRST_WORKOUT" -> R.string.achievement_first_workout_name
    "STREAK_7_TRAINING" -> R.string.achievement_streak_7_training_name
    "STREAK_30_TRAINING" -> R.string.achievement_streak_30_training_name
    "STREAK_7_NUTRITION" -> R.string.achievement_streak_7_nutrition_name
    "STREAK_30_NUTRITION" -> R.string.achievement_streak_30_nutrition_name
    "BENCH_60KG" -> R.string.achievement_bench_60kg_name
    "BENCH_100KG" -> R.string.achievement_bench_100kg_name
    "SQUAT_100KG" -> R.string.achievement_squat_100kg_name
    "DEADLIFT_140KG" -> R.string.achievement_deadlift_140kg_name
    "FIRST_PR" -> R.string.achievement_first_pr_name
    "CONSECUTIVE_4_WEEKS" -> R.string.achievement_consecutive_4_weeks_name
    "CONSECUTIVE_12_WEEKS" -> R.string.achievement_consecutive_12_weeks_name
    "WEIGHT_GOAL_REACHED" -> R.string.achievement_weight_goal_reached_name
    "KNOWLEDGE_INTERMEDIATE" -> R.string.achievement_knowledge_intermediate_name
    "KNOWLEDGE_ADVANCED" -> R.string.achievement_knowledge_advanced_name
    // Fallback local (LocalAchievementDefinitions)
    "FIRST_TRAINING_PLAN" -> R.string.achievement_first_training_plan_name
    "FIRST_DIET_PLAN" -> R.string.achievement_first_diet_plan_name
    "STREAK_7" -> R.string.achievement_streak_7_name
    "STREAK_30" -> R.string.achievement_streak_30_name
    "NUTRITION_7" -> R.string.achievement_nutrition_7_name
    "KNOWLEDGE_FIRST" -> R.string.achievement_knowledge_first_name
    "FIRST_PLAN_COMPLETE" -> R.string.achievement_first_plan_complete_name
    "PR_5" -> R.string.achievement_pr_5_name
    "WEIGHT_GOAL_PROGRESS" -> R.string.achievement_weight_goal_progress_name
    "STREAK_100" -> R.string.achievement_streak_100_name
    "STRENGTH_MASTER" -> R.string.achievement_strength_master_name
    else -> null
}

@StringRes
fun String.toAchievementDescriptionRes(): Int? = when (this) {
    "FIRST_WORKOUT" -> R.string.achievement_first_workout_desc
    "STREAK_7_TRAINING" -> R.string.achievement_streak_7_training_desc
    "STREAK_30_TRAINING" -> R.string.achievement_streak_30_training_desc
    "STREAK_7_NUTRITION" -> R.string.achievement_streak_7_nutrition_desc
    "STREAK_30_NUTRITION" -> R.string.achievement_streak_30_nutrition_desc
    "BENCH_60KG" -> R.string.achievement_bench_60kg_desc
    "BENCH_100KG" -> R.string.achievement_bench_100kg_desc
    "SQUAT_100KG" -> R.string.achievement_squat_100kg_desc
    "DEADLIFT_140KG" -> R.string.achievement_deadlift_140kg_desc
    "FIRST_PR" -> R.string.achievement_first_pr_desc
    "CONSECUTIVE_4_WEEKS" -> R.string.achievement_consecutive_4_weeks_desc
    "CONSECUTIVE_12_WEEKS" -> R.string.achievement_consecutive_12_weeks_desc
    "WEIGHT_GOAL_REACHED" -> R.string.achievement_weight_goal_reached_desc
    "KNOWLEDGE_INTERMEDIATE" -> R.string.achievement_knowledge_intermediate_desc
    "KNOWLEDGE_ADVANCED" -> R.string.achievement_knowledge_advanced_desc
    "FIRST_TRAINING_PLAN" -> R.string.achievement_first_training_plan_desc
    "FIRST_DIET_PLAN" -> R.string.achievement_first_diet_plan_desc
    "STREAK_7" -> R.string.achievement_streak_7_desc
    "STREAK_30" -> R.string.achievement_streak_30_desc
    "NUTRITION_7" -> R.string.achievement_nutrition_7_desc
    "KNOWLEDGE_FIRST" -> R.string.achievement_knowledge_first_desc
    "FIRST_PLAN_COMPLETE" -> R.string.achievement_first_plan_complete_desc
    "PR_5" -> R.string.achievement_pr_5_desc
    "WEIGHT_GOAL_PROGRESS" -> R.string.achievement_weight_goal_progress_desc
    "STREAK_100" -> R.string.achievement_streak_100_desc
    "STRENGTH_MASTER" -> R.string.achievement_strength_master_desc
    else -> null
}

fun Context.localizedAchievementName(code: String, fallback: String): String =
    code.toAchievementNameRes()?.let { getString(it) } ?: fallback

fun Context.localizedAchievementDescription(code: String, fallback: String): String =
    code.toAchievementDescriptionRes()?.let { getString(it) } ?: fallback

@Composable
fun AchievementDefinition.localizedName(): String {
    val resId = code.toAchievementNameRes()
    return if (resId != null) stringResource(resId) else name
}

@Composable
fun AchievementDefinition.localizedDescription(): String {
    val resId = code.toAchievementDescriptionRes()
    return if (resId != null) stringResource(resId) else description
}
