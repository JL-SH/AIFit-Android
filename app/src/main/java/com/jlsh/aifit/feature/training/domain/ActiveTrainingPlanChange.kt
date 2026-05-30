package com.jlsh.aifit.feature.training.domain

/**
 * Event payload when the active training plan changes.
 *
 * @param planId Target plan identifier.
 * @param planName Optional display name (used for optimistic updates before Room sync).
 * @param isOptimistic True when emitted before the activate API confirms.
 * @param isRevert True when activation failed and listeners should re-read Room.
 */
data class ActiveTrainingPlanChange(
    val planId: String,
    val planName: String? = null,
    val isOptimistic: Boolean = false,
    val isRevert: Boolean = false,
)
