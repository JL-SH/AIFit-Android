package com.jlsh.aifit.feature.workout.domain.util

import com.jlsh.aifit.feature.training.domain.model.MuscleGroup
import com.jlsh.aifit.feature.workout.domain.model.WorkoutSetLog
import com.jlsh.aifit.feature.workout.ui.state.SessionExercise
import kotlin.math.abs

/**
 * Estima el máximo de una repetición (1RM) con la fórmula de Epley simplificada.
 *
 * @param weight Peso levantado en la serie.
 * @param reps Repeticiones completadas; si es 0, devuelve [weight] sin ajuste.
 * @return 1RM estimado en las mismas unidades que [weight].
 */
fun calculateOneRepMax(weight: Double, reps: Int): Double {
    if (reps == 0) return weight
    return weight * (1 + reps / 30.0)
}

/**
 * Sugiere un peso ajustado cuando el RPE real se desvía del RPE objetivo.
 *
 * @param currentWeight Peso usado en la serie registrada.
 * @param actualRpe RPE percibido por el usuario (1–10).
 * @param targetRpe RPE prescrito para la serie.
 * @return Peso sugerido para la siguiente serie, o null si la desviación es ≤ 1 punto RPE.
 */
fun calculateAutoregulatedWeight(
    currentWeight: Double,
    actualRpe: Int,
    targetRpe: Int,
): Double? {
    if (abs(actualRpe - targetRpe) <= 1) return null
    val actualRIR = 10 - actualRpe
    val estimated1RM = currentWeight * (1 + actualRIR / 30.0)
    val targetRIR = 10 - targetRpe
    return estimated1RM / (1 + targetRIR / 30.0)
}

/**
 * Indica si un ejercicio ha completado todas sus series objetivo.
 *
 * @param completedSets Series ya registradas.
 * @param targetSets Series prescritas.
 */
fun isExerciseComplete(completedSets: Int, targetSets: Int): Boolean =
    completedSets >= targetSets

/**
 * Comprueba si todos los ejercicios de la sesión han completado sus series.
 *
 * @param exercises Lista de ejercicios de la sesión activa.
 * @return true si hay al menos un ejercicio y todos tienen [SessionExercise.isComplete].
 */
fun areAllExercisesComplete(exercises: List<SessionExercise>): Boolean =
    exercises.isNotEmpty() && exercises.all { it.isComplete }

/**
 * Índice del primer ejercicio con series pendientes, o el último si todos están completos.
 *
 * @param completedSets Series completadas por ejercicio (misma longitud que [targetSets]).
 * @param targetSets Series objetivo por ejercicio.
 * @return Índice del ejercicio actual en la lista de la sesión.
 * @throws IllegalArgumentException si las listas no tienen el mismo tamaño.
 */
fun resolveCurrentExerciseIndex(
    completedSets: List<Int>,
    targetSets: List<Int>,
): Int {
    require(completedSets.size == targetSets.size)
    if (completedSets.isEmpty()) return 0
    return completedSets.indices.firstOrNull { i ->
        !isExerciseComplete(completedSets[i], targetSets[i])
    } ?: completedSets.lastIndex
}

/**
 * Índice del siguiente ejercicio con series pendientes tras completar [justCompletedIndex].
 *
 * @param completedSets Series completadas por ejercicio.
 * @param targetSets Series objetivo por ejercicio.
 * @param justCompletedIndex Índice del ejercicio que acaba de completar todas sus series.
 * @return Índice del siguiente ejercicio incompleto, o null si no queda ninguno.
 * @throws IllegalArgumentException si las listas no tienen el mismo tamaño.
 */
fun resolveNextExerciseIndexAfterCompletion(
    completedSets: List<Int>,
    targetSets: List<Int>,
    justCompletedIndex: Int,
): Int? {
    require(completedSets.size == targetSets.size)
    return (justCompletedIndex + 1 until completedSets.size).firstOrNull { i ->
        !isExerciseComplete(completedSets[i], targetSets[i])
    }
}

/**
 * Suma el volumen (peso × reps) de las series que trabajan un grupo muscular dado.
 *
 * @param setLogs Series registradas en la sesión.
 * @param muscleGroup Grupo muscular a acumular.
 * @param exerciseMuscleMap Mapa de id de ejercicio de entrenamiento a [MuscleGroup].
 * @return Volumen total en unidades de peso × repeticiones.
 */
fun calculateAccumulatedVolume(
    setLogs: List<WorkoutSetLog>,
    muscleGroup: MuscleGroup,
    exerciseMuscleMap: Map<String, MuscleGroup>,
): Double {
    return setLogs
        .filter { exerciseMuscleMap[it.trainingExerciseId] == muscleGroup }
        .sumOf { set ->
            val reps = set.repsCompleted ?: 0
            val weight = set.weightUsed ?: 0.0
            reps * weight
        }
}
