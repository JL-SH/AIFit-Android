package com.jlsh.aifit.core.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween

object AiFitMotion {
    const val DurationShort = 200
    const val DurationMedium = 300
    const val DurationLong = 500
    const val DurationMetricCountUp = 600

    val StandardEasing = FastOutSlowInEasing

    fun <T> standardTween(durationMillis: Int = DurationMedium): TweenSpec<T> =
        tween(durationMillis = durationMillis, easing = StandardEasing)

    fun <T> metricCountUpTween(): TweenSpec<T> =
        tween(durationMillis = DurationMetricCountUp, easing = StandardEasing)
}
