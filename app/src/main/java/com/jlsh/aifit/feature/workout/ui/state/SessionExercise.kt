package com.jlsh.aifit.feature.workout.ui.state

import com.jlsh.aifit.feature.training.domain.model.MuscleGroup

/**
 * Representation of an exercise within an active training session.
 *
 * @property exerciseId Identifier of the exercise in the training plan.
 * @property name Display name of the exercise (may change after substitution).
 * @property primaryMuscle Primary muscle group targeted.
 * @property targetSets Prescribed series for the exercise.
 * @property targetReps Target reps (prescribed minimum).
 * @property targetRpe Optional target RPE for self-regulation.
 * @property restSeconds Base rest between sets in seconds.
 * @property completedSets Series already registered in this session.
 * @property requiresExternalWeight If true, the form requires weight in kilograms.
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
