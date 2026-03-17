package com.jlsh.aifit.feature.workout.domain.util

import com.jlsh.aifit.feature.training.domain.model.MuscleGroup
import com.jlsh.aifit.feature.workout.domain.model.WorkoutSetLog
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

