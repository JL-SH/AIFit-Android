package com.jlsh.aifit.feature.workout.domain.util

import com.jlsh.aifit.feature.workout.domain.model.WorkoutSetLog
import com.jlsh.aifit.testutil.fakeTrainingExercise
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutSessionStatsTest {

    @Test
    fun `counts completed exercises and plan total sets`() {
        val exA = fakeTrainingExercise(id = "ex-a", sets = 4)
        val exB = fakeTrainingExercise(id = "ex-b", sets = 5)
        val loggedSets = listOf(
            setLog("ex-a", 1),
            setLog("ex-a", 2),
            setLog("ex-a", 3),
            setLog("ex-a", 4),
            setLog("ex-b", 1),
        )

        val stats = computeWorkoutSessionStats(
            planExercises = listOf(exA, exB),
            loggedSets = loggedSets,
        )

        assertEquals(2, stats.planExerciseCount)
        assertEquals(1, stats.completedExercises)
        assertEquals(9, stats.planTotalSets)
        assertEquals(5, stats.loggedCompletedSets)
        assertEquals(4, stats.targetSetsByExerciseId["ex-a"])
        assertEquals(5, stats.targetSetsByExerciseId["ex-b"])
    }

    private fun setLog(exerciseId: String, setNumber: Int) = WorkoutSetLog(
        id = "$exerciseId-$setNumber",
        trainingExerciseId = exerciseId,
        exerciseName = "Exercise",
        exerciseSetNumber = setNumber,
        repsCompleted = 10,
        weightUsed = 50.0,
        durationSeconds = null,
        completed = true,
        estimatedOneRepMax = null,
        wasAutoregulated = false,
        rpe = null,
    )
}
