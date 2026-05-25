package com.jlsh.aifit.feature.workout.domain.util

import com.jlsh.aifit.feature.training.domain.model.TrainingExercise
import com.jlsh.aifit.feature.workout.domain.model.WorkoutSetLog

/**
 * Métricas de sesión alineadas al plan del día (objetivos) frente a lo registrado en el log.
 */
data class WorkoutSessionStats(
    val planExerciseCount: Int,
    val completedExercises: Int,
    val planTotalSets: Int,
    val loggedCompletedSets: Int,
    val targetSetsByExerciseId: Map<String, Int>,
)

/**
 * Calcula ejercicios y series completadas según el plan y las series guardadas en el log.
 */
fun computeWorkoutSessionStats(
    planExercises: List<TrainingExercise>,
    loggedSets: List<WorkoutSetLog>,
): WorkoutSessionStats {
    val targetSetsByExerciseId = planExercises.associate { it.id to it.sets }
    val loggedCountByExercise = loggedSets
        .groupingBy { it.trainingExerciseId }
        .eachCount()

    val completedExercises = planExercises.count { exercise ->
        val logged = loggedCountByExercise[exercise.id] ?: 0
        isExerciseComplete(logged, exercise.sets)
    }

    return WorkoutSessionStats(
        planExerciseCount = planExercises.size,
        completedExercises = completedExercises,
        planTotalSets = planExercises.sumOf { it.sets },
        loggedCompletedSets = loggedSets.count { it.completed },
        targetSetsByExerciseId = targetSetsByExerciseId,
    )
}
