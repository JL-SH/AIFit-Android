package com.jlsh.aifit.feature.workout.domain.util

import com.jlsh.aifit.feature.training.domain.model.MuscleGroup
import com.jlsh.aifit.feature.workout.domain.model.WorkoutSetLog
import com.jlsh.aifit.feature.workout.ui.state.SessionExercise
import kotlin.math.abs

fun calculateOneRepMax(weight: Double, reps: Int): Double {
    if (reps == 0) return weight
    return weight * (1 + reps / 30.0)
}

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

fun isExerciseComplete(completedSets: Int, targetSets: Int): Boolean =
    completedSets >= targetSets

fun areAllExercisesComplete(exercises: List<SessionExercise>): Boolean =
    exercises.isNotEmpty() && exercises.all { it.isComplete }

/**
 * Index of the first exercise with remaining sets, or the last exercise if all are complete.
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
 * Returns the index of the next exercise with remaining sets after [justCompletedIndex],
 * or null if every exercise from that point onward is complete.
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

