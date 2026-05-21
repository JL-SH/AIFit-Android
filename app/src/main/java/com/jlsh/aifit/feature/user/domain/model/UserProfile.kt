package com.jlsh.aifit.feature.user.domain.model

import java.time.LocalDate

/**
 * Perfil de usuario con datos de identidad, antropometría y preferencias de entrenamiento/nutrición.
 *
 * @property id Identificador único del usuario.
 * @property name Nombre para mostrar.
 * @property email Correo de la cuenta.
 * @property authProvider Proveedor de autenticación (p. ej. `LOCAL`, `GOOGLE`).
 * @property profilePictureUrl URL del avatar, si existe.
 * @property birthDate Fecha de nacimiento.
 * @property gender Género declarado.
 * @property height Altura en centímetros.
 * @property weight Peso actual en kilogramos.
 * @property targetWeight Peso objetivo en kilogramos.
 * @property goalType Objetivo principal (pérdida de grasa, ganancia muscular, etc.).
 * @property activityLevel Nivel de actividad diaria.
 * @property fitnessLevel Nivel de experiencia en entrenamiento.
 * @property workoutLocation Lugar preferido para entrenar.
 * @property dietPreference Preferencia dietética.
 * @property knowledgeLevel Nivel de conocimiento en fitness/nutrición (texto libre del backend).
 * @property weeklyWorkoutDays Días de entrenamiento por semana.
 * @property availableMinutesPerSession Minutos disponibles por sesión.
 * @property injuries Lesiones o limitaciones indicadas por el usuario.
 * @property calorieTarget Objetivo calórico diario en kcal.
 */
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val authProvider: String,
    val profilePictureUrl: String? = null,
    val birthDate: LocalDate? = null,
    val gender: Gender? = null,
    val height: Float? = null,
    val weight: Float? = null,
    val targetWeight: Float? = null,
    val goalType: GoalType? = null,
    val activityLevel: ActivityLevel? = null,
    val fitnessLevel: FitnessLevel? = null,
    val workoutLocation: WorkoutLocation? = null,
    val dietPreference: DietPreference? = null,
    val knowledgeLevel: String? = null,
    val weeklyWorkoutDays: Int? = null,
    val availableMinutesPerSession: Int? = null,
    val injuries: String? = null,
    val calorieTarget: Int? = null,
)

