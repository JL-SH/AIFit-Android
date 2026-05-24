package com.jlsh.aifit.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.jlsh.aifit.core.ui.theme.AiFitMotion

enum class NavTransitionType {
    Push,
    Modal,
    Fade,
}

private const val NAV_DURATION_MS = AiFitMotion.DurationMedium
private val NAV_EASING = FastOutSlowInEasing

private fun pushEnter(): EnterTransition =
    fadeIn(tween(NAV_DURATION_MS, easing = NAV_EASING)) +
        slideInHorizontally(
            animationSpec = tween(NAV_DURATION_MS, easing = NAV_EASING),
            initialOffsetX = { fullWidth -> fullWidth / 4 },
        )

private fun pushExit(): ExitTransition =
    fadeOut(tween(NAV_DURATION_MS, easing = NAV_EASING)) +
        slideOutHorizontally(
            animationSpec = tween(NAV_DURATION_MS, easing = NAV_EASING),
            targetOffsetX = { fullWidth -> -fullWidth / 4 },
        )

private fun pushPopEnter(): EnterTransition =
    fadeIn(tween(NAV_DURATION_MS, easing = NAV_EASING)) +
        slideInHorizontally(
            animationSpec = tween(NAV_DURATION_MS, easing = NAV_EASING),
            initialOffsetX = { fullWidth -> -fullWidth / 4 },
        )

private fun pushPopExit(): ExitTransition =
    fadeOut(tween(NAV_DURATION_MS, easing = NAV_EASING)) +
        slideOutHorizontally(
            animationSpec = tween(NAV_DURATION_MS, easing = NAV_EASING),
            targetOffsetX = { fullWidth -> fullWidth / 4 },
        )

private fun modalEnter(): EnterTransition =
    fadeIn(tween(NAV_DURATION_MS, easing = NAV_EASING)) +
        slideInVertically(
            animationSpec = tween(NAV_DURATION_MS, easing = NAV_EASING),
            initialOffsetY = { fullHeight -> fullHeight / 8 },
        )

private fun modalExit(): ExitTransition =
    fadeOut(tween(NAV_DURATION_MS, easing = NAV_EASING)) +
        slideOutVertically(
            animationSpec = tween(NAV_DURATION_MS, easing = NAV_EASING),
            targetOffsetY = { fullHeight -> fullHeight / 8 },
        )

private fun fadeEnter(): EnterTransition =
    fadeIn(tween(AiFitMotion.DurationShort, easing = NAV_EASING))

private fun fadeExit(): ExitTransition =
    fadeOut(tween(AiFitMotion.DurationShort, easing = NAV_EASING))

private val modalRoutePrefixes = listOf(
    "training/generate",
    "training/approval/",
    "training/session/",
    "training/workout_log",
    "nutrition/diet_generate",
    "nutrition/diet/approval/",
    "nutrition/track_meal",
    "nutrition/food_vision",
    "coach/new_chat",
    "coach/chat/",
    "home/body_weight",
    "profile/edit",
)

fun routeTransitionType(route: String): NavTransitionType {
    val normalized = route.substringBefore("?")
    if (modalRoutePrefixes.any { normalized.startsWith(it) }) {
        return NavTransitionType.Modal
    }
    if (
        normalized.endsWith("_graph") ||
        normalized == HomeRoutes.HOME ||
        normalized == TrainingRoutes.HUB ||
        normalized == NutritionRoutes.HUB ||
        normalized == CoachRoutes.SESSION_LIST ||
        normalized == ProfileRoutes.HUB
    ) {
        return NavTransitionType.Fade
    }
    return NavTransitionType.Push
}

fun NavGraphBuilder.aifitComposable(
    route: String,
    transitionType: NavTransitionType = routeTransitionType(route),
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = {
            when (transitionType) {
                NavTransitionType.Push -> pushEnter()
                NavTransitionType.Modal -> modalEnter()
                NavTransitionType.Fade -> fadeEnter()
            }
        },
        exitTransition = {
            when (transitionType) {
                NavTransitionType.Push -> pushExit()
                NavTransitionType.Modal -> modalExit()
                NavTransitionType.Fade -> fadeExit()
            }
        },
        popEnterTransition = {
            when (transitionType) {
                NavTransitionType.Push -> pushPopEnter()
                NavTransitionType.Modal -> modalEnter()
                NavTransitionType.Fade -> fadeEnter()
            }
        },
        popExitTransition = {
            when (transitionType) {
                NavTransitionType.Push -> pushPopExit()
                NavTransitionType.Modal -> modalExit()
                NavTransitionType.Fade -> fadeExit()
            }
        },
        content = content,
    )
}
