package com.jlsh.aifit.feature.workout.domain.util

import com.jlsh.aifit.feature.training.domain.model.TrainingExercise
import com.jlsh.aifit.feature.workout.domain.model.WorkoutSetLog

/**
 * Session metrics aligned to the day's plan (objectives) versus what is recorded in the log.
 */
data class WorkoutSessionStats(
    val planExerciseCount: Int,
    val completedExercises: Int,
    val planTotalSets: Int,
    val loggedCompletedSets: Int,
    val targetSetsByExerciseId: Map<String, Int>,
)

/**
 * Calculates exercises and completed series according to the plan and the series saved in the log.
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
