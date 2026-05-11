package com.jlsh.aifit.feature.user.ui

import com.jlsh.aifit.feature.diet.domain.model.MealType
import com.jlsh.aifit.feature.education.domain.model.KnowledgeLevel
import com.jlsh.aifit.feature.gamification.domain.model.AchievementRarity
import com.jlsh.aifit.feature.gamification.domain.model.ExportPeriod
import com.jlsh.aifit.feature.progression.domain.model.ProgressionType
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingListPeriod
import com.jlsh.aifit.feature.user.domain.model.ActivityLevel
import com.jlsh.aifit.feature.user.domain.model.DietPreference
import com.jlsh.aifit.feature.user.domain.model.FitnessLevel
import com.jlsh.aifit.feature.user.domain.model.Gender
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.WorkoutLocation
import androidx.annotation.StringRes
import com.jlsh.aifit.R
import com.jlsh.aifit.feature.training.domain.model.MuscleGroup

fun GoalType.displayName(): String = when (this) {
    GoalType.LOSE_WEIGHT -> "Perder peso"
    GoalType.GAIN_MUSCLE -> "Ganar músculo"
    GoalType.MAINTAIN -> "Mantener"
    GoalType.BODY_RECOMPOSITION -> "Recomposición corporal"
    GoalType.UNKNOWN -> "Desconocido"
}

@StringRes
fun GoalType.toStringRes(): Int = when (this) {
    GoalType.LOSE_WEIGHT -> R.string.goal_type_lose_weight
    GoalType.GAIN_MUSCLE -> R.string.goal_type_gain_muscle
    GoalType.MAINTAIN -> R.string.goal_type_maintain
    GoalType.BODY_RECOMPOSITION -> R.string.goal_type_body_recomposition
    GoalType.UNKNOWN -> R.string.goal_type_unknown
}

fun FitnessLevel.displayName(): String = when (this) {
    FitnessLevel.BEGINNER -> "Principiante"
    FitnessLevel.INTERMEDIATE -> "Intermedio"
    FitnessLevel.ADVANCED -> "Avanzado"
    FitnessLevel.UNKNOWN -> "Desconocido"
}

@StringRes
fun FitnessLevel.toStringRes(): Int = when (this) {
    FitnessLevel.BEGINNER -> R.string.fitness_level_beginner
    FitnessLevel.INTERMEDIATE -> R.string.fitness_level_intermediate
    FitnessLevel.ADVANCED -> R.string.fitness_level_advanced
    FitnessLevel.UNKNOWN -> R.string.fitness_level_unknown
}

fun ActivityLevel.displayName(): String = when (this) {
    ActivityLevel.SEDENTARY -> "Sedentario"
    ActivityLevel.LIGHT -> "Ligeramente activo"
    ActivityLevel.MODERATE -> "Moderadamente activo"
    ActivityLevel.ACTIVE -> "Activo"
    ActivityLevel.VERY_ACTIVE -> "Muy activo"
    ActivityLevel.UNKNOWN -> "Desconocido"
}

@StringRes
fun ActivityLevel.toStringRes(): Int = when (this) {
    ActivityLevel.SEDENTARY -> R.string.activity_level_sedentary
    ActivityLevel.LIGHT -> R.string.activity_level_light
    ActivityLevel.MODERATE -> R.string.activity_level_moderate
    ActivityLevel.ACTIVE -> R.string.activity_level_active
    ActivityLevel.VERY_ACTIVE -> R.string.activity_level_very_active
    ActivityLevel.UNKNOWN -> R.string.activity_level_unknown
}

fun Gender.displayName(): String = when (this) {
    Gender.MALE -> "Masculino"
    Gender.FEMALE -> "Femenino"
    Gender.OTHER -> "Otro"
    Gender.UNKNOWN -> "Desconocido"
}

@StringRes
fun Gender.toStringRes(): Int = when (this) {
    Gender.MALE -> R.string.gender_male
    Gender.FEMALE -> R.string.gender_female
    Gender.OTHER -> R.string.gender_other
    Gender.UNKNOWN -> R.string.gender_unknown
}

fun WorkoutLocation.displayName(): String = when (this) {
    WorkoutLocation.GYM -> "Gimnasio"
    WorkoutLocation.HOME -> "Casa"
    WorkoutLocation.OUTDOOR -> "Exterior"
    WorkoutLocation.HOME_GYM -> "Gimnasio en casa"
    WorkoutLocation.UNKNOWN -> "Desconocido"
}

@StringRes
fun WorkoutLocation.toStringRes(): Int = when (this) {
    WorkoutLocation.GYM -> R.string.workout_location_gym
    WorkoutLocation.HOME -> R.string.workout_location_home
    WorkoutLocation.OUTDOOR -> R.string.workout_location_outdoor
    WorkoutLocation.HOME_GYM -> R.string.workout_location_home_gym
    WorkoutLocation.UNKNOWN -> R.string.workout_location_unknown
}

fun DietPreference.displayName(): String = when (this) {
    DietPreference.NONE -> "Sin preferencia"
    DietPreference.VEGETARIAN -> "Vegetariano"
    DietPreference.VEGAN -> "Vegano"
    DietPreference.KETO -> "Keto"
    DietPreference.PALEO -> "Paleo"
    DietPreference.GLUTEN_FREE -> "Sin gluten"
    DietPreference.LACTOSE_FREE -> "Sin lactosa"
    DietPreference.MEDITERRANEAN -> "Mediterráneo"
    DietPreference.UNKNOWN -> "Desconocido"
}

@StringRes
fun DietPreference.toStringRes(): Int = when (this) {
    DietPreference.NONE -> R.string.diet_pref_none
    DietPreference.VEGETARIAN -> R.string.diet_pref_vegetarian
    DietPreference.VEGAN -> R.string.diet_pref_vegan
    DietPreference.KETO -> R.string.diet_pref_keto
    DietPreference.PALEO -> R.string.diet_pref_paleo
    DietPreference.GLUTEN_FREE -> R.string.diet_pref_gluten_free
    DietPreference.LACTOSE_FREE -> R.string.diet_pref_lactose_free
    DietPreference.MEDITERRANEAN -> R.string.diet_pref_mediterranean
    DietPreference.UNKNOWN -> R.string.diet_pref_unknown
}

fun String.toGoalTypeDisplay(): String =
    runCatching { GoalType.valueOf(this).displayName() }.getOrDefault(this)

fun String.toFitnessLevelDisplay(): String =
    runCatching { FitnessLevel.valueOf(this).displayName() }.getOrDefault(this)

fun String.toActivityLevelDisplay(): String =
    runCatching { ActivityLevel.valueOf(this).displayName() }.getOrDefault(this)

fun String.toGenderDisplay(): String =
    runCatching { Gender.valueOf(this).displayName() }.getOrDefault(this)

fun String.toPreferredLocationDisplay(): String =
    runCatching { WorkoutLocation.valueOf(this).displayName() }.getOrDefault(this)

fun String.toDietPreferenceDisplay(): String =
    runCatching { DietPreference.valueOf(this).displayName() }.getOrDefault(this)

fun MuscleGroup.displayName(): String = when (this) {
    MuscleGroup.CHEST -> "Pecho"
    MuscleGroup.BACK -> "Espalda"
    MuscleGroup.SHOULDERS -> "Hombros"
    MuscleGroup.BICEPS -> "Bíceps"
    MuscleGroup.TRICEPS -> "Tríceps"
    MuscleGroup.FOREARMS -> "Antebrazos"
    MuscleGroup.LEGS -> "Piernas"
    MuscleGroup.GLUTES -> "Glúteos"
    MuscleGroup.CORE -> "Core"
    MuscleGroup.FULL_BODY -> "Cuerpo completo"
    MuscleGroup.CARDIO -> "Cardio"
    MuscleGroup.UNKNOWN -> "\u2014"
}

@StringRes
fun MuscleGroup.toStringRes(): Int = when (this) {
    MuscleGroup.CHEST -> R.string.muscle_group_chest
    MuscleGroup.BACK -> R.string.muscle_group_back
    MuscleGroup.SHOULDERS -> R.string.muscle_group_shoulders
    MuscleGroup.BICEPS -> R.string.muscle_group_biceps
    MuscleGroup.TRICEPS -> R.string.muscle_group_triceps
    MuscleGroup.FOREARMS -> R.string.muscle_group_forearms
    MuscleGroup.LEGS -> R.string.muscle_group_legs
    MuscleGroup.GLUTES -> R.string.muscle_group_glutes
    MuscleGroup.CORE -> R.string.muscle_group_core
    MuscleGroup.FULL_BODY -> R.string.muscle_group_full_body
    MuscleGroup.CARDIO -> R.string.muscle_group_cardio
    MuscleGroup.UNKNOWN -> R.string.muscle_group_unknown
}

fun MealType.displayName(): String = when (this) {
    MealType.BREAKFAST -> "Desayuno"
    MealType.MID_MORNING -> "Media mañana"
    MealType.LUNCH -> "Comida"
    MealType.AFTERNOON_SNACK -> "Merienda"
    MealType.DINNER -> "Cena"
    MealType.PRE_WORKOUT -> "Pre-entrenamiento"
    MealType.POST_WORKOUT -> "Post-entrenamiento"
    MealType.UNKNOWN -> "—"
}

@StringRes
fun MealType.toStringRes(): Int = when (this) {
    MealType.BREAKFAST -> R.string.meal_type_breakfast
    MealType.MID_MORNING -> R.string.meal_type_morning_snack
    MealType.LUNCH -> R.string.meal_type_lunch
    MealType.AFTERNOON_SNACK -> R.string.meal_type_snack
    MealType.DINNER -> R.string.meal_type_dinner
    MealType.PRE_WORKOUT -> R.string.meal_type_pre_workout
    MealType.POST_WORKOUT -> R.string.meal_type_post_workout
    MealType.UNKNOWN -> R.string.meal_type_unknown
}

@StringRes
fun String.toMealTypeStringRes(): Int =
    runCatching { MealType.valueOf(this).toStringRes() }.getOrDefault(R.string.meal_type_unknown)

@StringRes
fun AchievementRarity.toStringRes(): Int = when (this) {
    AchievementRarity.COMMON     -> R.string.achievement_rarity_common
    AchievementRarity.UNCOMMON   -> R.string.achievement_rarity_uncommon
    AchievementRarity.RARE       -> R.string.achievement_rarity_rare
    AchievementRarity.LEGENDARY  -> R.string.achievement_rarity_legendary
    AchievementRarity.UNKNOWN    -> R.string.achievement_rarity_unknown
}

@StringRes
fun ProgressionType.toStringRes(): Int = when (this) {
    ProgressionType.INCREASE_LOAD      -> R.string.progression_type_increase_load
    ProgressionType.MAINTAIN_LOAD      -> R.string.progression_type_maintain_load
    ProgressionType.DECREASE_REPS      -> R.string.progression_type_decrease_reps
    ProgressionType.DELOAD             -> R.string.progression_type_deload
    ProgressionType.CHANGE_REP_RANGE   -> R.string.progression_type_change_rep_range
    ProgressionType.INSUFFICIENT_DATA  -> R.string.progression_type_insufficient_data
    ProgressionType.UNKNOWN            -> R.string.progression_type_unknown
}

@StringRes
fun ShoppingListPeriod.toStringRes(): Int = when (this) {
    ShoppingListPeriod.ONE_WEEK   -> R.string.shopping_period_one_week
    ShoppingListPeriod.TWO_WEEKS  -> R.string.shopping_period_two_weeks
    ShoppingListPeriod.ONE_MONTH  -> R.string.shopping_period_one_month
    ShoppingListPeriod.UNKNOWN    -> R.string.shopping_period_unknown
}

@StringRes
fun ExportPeriod.toStringRes(): Int = when (this) {
    ExportPeriod.LAST_WEEK         -> R.string.export_period_last_week
    ExportPeriod.LAST_MONTH        -> R.string.export_period_last_month
    ExportPeriod.LAST_THREE_MONTHS -> R.string.export_period_last_3_months
    ExportPeriod.ALL_TIME          -> R.string.export_period_all_time
}

@StringRes
fun KnowledgeLevel.toStringRes(): Int = when (this) {
    KnowledgeLevel.BEGINNER     -> R.string.knowledge_level_beginner
    KnowledgeLevel.INTERMEDIATE -> R.string.knowledge_level_intermediate
    KnowledgeLevel.ADVANCED     -> R.string.knowledge_level_advanced
    KnowledgeLevel.UNKNOWN      -> R.string.knowledge_level_unknown
}
