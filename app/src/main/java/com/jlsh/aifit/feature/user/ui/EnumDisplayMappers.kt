package com.jlsh.aifit.feature.user.ui

import com.jlsh.aifit.feature.user.domain.model.ActivityLevel
import com.jlsh.aifit.feature.user.domain.model.DietPreference
import com.jlsh.aifit.feature.user.domain.model.FitnessLevel
import com.jlsh.aifit.feature.user.domain.model.Gender
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.WorkoutLocation

fun GoalType.displayName(): String = when (this) {
    GoalType.LOSE_WEIGHT -> "Perder peso"
    GoalType.GAIN_MUSCLE -> "Ganar músculo"
    GoalType.MAINTAIN -> "Mantener"
    GoalType.BODY_RECOMPOSITION -> "Recomposición corporal"
    GoalType.UNKNOWN -> "Desconocido"
}

fun FitnessLevel.displayName(): String = when (this) {
    FitnessLevel.BEGINNER -> "Principiante"
    FitnessLevel.INTERMEDIATE -> "Intermedio"
    FitnessLevel.ADVANCED -> "Avanzado"
    FitnessLevel.UNKNOWN -> "Desconocido"
}

fun ActivityLevel.displayName(): String = when (this) {
    ActivityLevel.SEDENTARY -> "Sedentario"
    ActivityLevel.LIGHT -> "Ligeramente activo"
    ActivityLevel.MODERATE -> "Moderadamente activo"
    ActivityLevel.ACTIVE -> "Activo"
    ActivityLevel.VERY_ACTIVE -> "Muy activo"
    ActivityLevel.UNKNOWN -> "Desconocido"
}

fun Gender.displayName(): String = when (this) {
    Gender.MALE -> "Masculino"
    Gender.FEMALE -> "Femenino"
    Gender.OTHER -> "Otro"
    Gender.UNKNOWN -> "Desconocido"
}

fun WorkoutLocation.displayName(): String = when (this) {
    WorkoutLocation.GYM -> "Gimnasio"
    WorkoutLocation.HOME -> "Casa"
    WorkoutLocation.OUTDOOR -> "Exterior"
    WorkoutLocation.HOME_GYM -> "Gimnasio en casa"
    WorkoutLocation.UNKNOWN -> "Desconocido"
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

