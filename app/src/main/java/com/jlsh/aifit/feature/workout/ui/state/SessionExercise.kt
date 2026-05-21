package com.jlsh.aifit.feature.workout.ui.state

import com.jlsh.aifit.feature.training.domain.model.MuscleGroup

data class SessionExercise(
    val exerciseId: String,
    val name: String,
    val primaryMuscle: MuscleGroup,
    val targetSets: Int,
    val targetReps: Int,
    val targetRpe: Int?,
    val restSeconds: Int,
    val completedSets: Int,
) {
    val isComplete: Boolean get() = completedSets >= targetSets
}

