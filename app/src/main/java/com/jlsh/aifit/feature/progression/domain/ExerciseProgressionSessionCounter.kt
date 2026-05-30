package com.jlsh.aifit.feature.progression.domain

/**
 * Counts how many distinct workout sessions include logged sets for an exercise.
 * Mirrors backend [ProgressionUseCaseHelper] session grouping (one session per log date).
 */
object ExerciseProgressionSessionCounter {

    fun countDistinctSessions(logDates: List<String>): Int = logDates.distinct().size
}
