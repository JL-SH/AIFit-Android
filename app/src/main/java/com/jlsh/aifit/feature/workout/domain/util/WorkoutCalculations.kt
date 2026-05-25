package com.jlsh.aifit.feature.workout.domain.util

import com.jlsh.aifit.feature.training.domain.model.MuscleGroup
import com.jlsh.aifit.feature.workout.domain.model.WorkoutSetLog
import com.jlsh.aifit.feature.workout.ui.state.SessionExercise
import kotlin.math.abs

/**
 * Estimate your one repetition maximum (1RM) with the simplified Epley formula.
 *
 * @param weight Weight lifted in the set.
 * @param reps Reps completed; if 0, returns [weight] without adjustment.
 * @return 1RM estimado in the same units as [weight].
 */
fun calculateOneRepMax(weight: Double, reps: Int): Double {
    if (reps == 0) return weight
    return weight * (1 + reps / 30.0)
}

/**
 * Suggests adjusted weight when actual RPE deviates from target RPE.
 *
 * @param currentWeight Weight used in the recorded series.
 * @param currentRpe User perceived RPE (1–10).
 * @param targetRpe Prescribed RPE for the series.
 * @return Suggested weight for the next set, or null if the deviation is ≤ 1 RPE point.
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
 * Indicates whether an exercise has completed all of its target sets.
 *
     * @param completedSets Sets already logged.
 * @param targetSets Series prescritas.
 */
fun isExerciseComplete(completedSets: Int, targetSets: Int): Boolean =
    completedSets >= targetSets

/**
 * Check if all the exercises in the session have completed their series.
 *
 * @param exercises List of exercises for the active session.
 * @return true if there is at least one exercise and they all have [SessionExercise.isComplete].
 */
fun areAllExercisesComplete(exercises: List<SessionExercise>): Boolean =
    exercises.isNotEmpty() && exercises.all { it.isComplete }

/**
 * Index of the first exercise with pending series, or the last if all are complete.
 *
 * @param completedSets Series completed per exercise (same length as [targetSets]).
 * @param targetSets Target series per exercise.
 * @return Index of the current exercise in the session list.
 * @throws IllegalArgumentException if the lists are not the same size.
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
 * Index of the next exercise with pending series after completing [justCompletedIndex].
 *
 * @param completedSets Completed sets per exercise.
 * @param targetSets Target series per exercise.
 * @param justCompletedIndex Index of the exercise that has just completed all its sets.
 * @return Index of the next incomplete exercise, or null if none remain.
 * @throws IllegalArgumentException if the lists are not the same size.
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
 * Sums volume (weight × reps) for sets that target a given muscle group.
 *
 * @param setLogs Series logged in the session.
 * @param muscleGroup Muscle group to accumulate volume for.
 * @param exerciseMuscleMap Map training exercise id to [MuscleGroup].
 * @return Total volume in weight × reps units.
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
