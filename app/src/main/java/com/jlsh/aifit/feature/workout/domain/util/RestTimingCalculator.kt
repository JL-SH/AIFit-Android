package com.jlsh.aifit.feature.workout.domain.util

private val REST_MEDIUM = 120
private val REST_HIGH = 180

fun calculateRestSeconds(rpe: Int, baseRestSeconds: Int): Int {
    return when {
        rpe < 6 -> baseRestSeconds
        rpe < 8 -> REST_MEDIUM
        else -> REST_HIGH
    }
}

