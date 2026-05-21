package com.jlsh.aifit.feature.workout.ui.state

import com.jlsh.aifit.feature.training.domain.model.MuscleGroup

/**
 * Representación de un ejercicio dentro de una sesión de entrenamiento activa.
 *
 * @property exerciseId Identificador del ejercicio en el plan de entrenamiento.
 * @property name Nombre mostrado del ejercicio (puede cambiar tras sustitución).
 * @property primaryMuscle Grupo muscular principal trabajado.
 * @property targetSets Series prescritas para el ejercicio.
 * @property targetReps Repeticiones objetivo (mínimo prescrito).
 * @property targetRpe RPE objetivo opcional para autoregulación.
 * @property restSeconds Descanso base entre series en segundos.
 * @property completedSets Series ya registradas en esta sesión.
 * @property requiresExternalWeight Si true, el formulario exige peso en kilogramos.
 */
data class SessionExercise(
    val exerciseId: String,
    val name: String,
    val primaryMuscle: MuscleGroup,
    val targetSets: Int,
    val targetReps: Int,
    val targetRpe: Int?,
    val restSeconds: Int,
    val completedSets: Int,
    val requiresExternalWeight: Boolean = true,
) {
    /** true cuando [completedSets] alcanza o supera [targetSets]. */
    val isComplete: Boolean get() = completedSets >= targetSets
}
