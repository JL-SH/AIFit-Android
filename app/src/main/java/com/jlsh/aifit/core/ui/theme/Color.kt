package com.jlsh.aifit.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── Gradient anchors (theme-independent) ─────────────────────────────────────

val GradientGoldStart = Color(0xFFF59E0B)
val GradientGoldEnd = Color(0xFFFBBF24)
val GradientDangerStart = Color(0xFFB51925)
val GradientDangerEnd = Color(0xFFFF5353)

private val DiagonalGradientEnd = Offset(1000f, 1000f)

/** Lightens the color toward white by [factor] (e.g. 0.2 = +20% luminosity). */
fun Color.lighten(factor: Float): Color {
    return Color(
        red = (red + (1f - red) * factor).coerceIn(0f, 1f),
        green = (green + (1f - green) * factor).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * factor).coerceIn(0f, 1f),
        alpha = alpha,
    )
}

// ── Light scheme ─────────────────────────────────────────────────────────────

val LightPrimary = Color(0xFF5B7300)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFD4F94A)
val LightOnPrimaryContainer = Color(0xFF1A2200)
val LightSecondary = Color(0xFF0891B2)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFE0F7FA)
val LightOnSecondaryContainer = Color(0xFF003F4A)
val LightTertiary = Color(0xFF006877)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFF22D3EE)
val LightOnTertiaryContainer = Color(0xFF003F4A)
val LightError = Color(0xFFB51925)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFD8363A)
val LightOnErrorContainer = Color(0xFFFFFFFF)
val LightBackground = Color(0xFFF4F5F0)
val LightOnBackground = Color(0xFF1A1F14)
val LightSurface = Color(0xFFFAFBF7)
val LightOnSurface = Color(0xFF1A1F14)
val LightSurfaceVariant = Color(0xFFE4E8DC)
val LightOnSurfaceVariant = Color(0xFF3D4540)
val LightOutline = Color(0xFF5C6560)
val LightOutlineVariant = Color(0xFFC4CCC6)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFF0F2EB)
val LightSurfaceContainer = Color(0xFFE8EBE3)
val LightSurfaceContainerHigh = Color(0xFFE0E4DA)
val LightSurfaceContainerHighest = Color(0xFFD8DDD2)
val LightInverseSurface = Color(0xFF1A1F14)
val LightInverseOnSurface = Color(0xFFF4F5F0)
val LightInversePrimary = Color(0xFFB8E635)
val LightScrim = Color(0xFF000000)

// ── Dark scheme ──────────────────────────────────────────────────────────────

val DarkPrimary = Color(0xFFB8E635)
val DarkOnPrimary = Color(0xFF0A0D12)
val DarkPrimaryContainer = Color(0xFFD4F94A)
val DarkOnPrimaryContainer = Color(0xFF1A2200)
val DarkSecondary = Color(0xFF5CE1E6)
val DarkOnSecondary = Color(0xFF0A0D12)
val DarkSecondaryContainer = Color(0xFF0F1923)
val DarkOnSecondaryContainer = Color(0xFF5CE1E6)
val DarkTertiary = Color(0xFF7DD3FC)
val DarkOnTertiary = Color(0xFF00363E)
val DarkTertiaryContainer = Color(0xFF22D3EE)
val DarkOnTertiaryContainer = Color(0xFF003F4A)
val DarkError = Color(0xFFFFB3AE)
val DarkOnError = Color(0xFF68000C)
val DarkErrorContainer = Color(0xFFFF5353)
val DarkOnErrorContainer = Color(0xFF410002)
val DarkBackground = Color(0xFF0D0F14)
val DarkOnBackground = Color(0xFFE8EAED)
val DarkSurface = Color(0xFF12151C)
val DarkOnSurface = Color(0xFFE8EAED)
val DarkSurfaceVariant = Color(0xFF3D4454)
val DarkOnSurfaceVariant = Color(0xFFB8BEC8)
val DarkOutline = Color(0xFF8E95A3)
val DarkOutlineVariant = Color(0xFF3D4454)
val DarkSurfaceContainerLowest = Color(0xFF0A0C10)
val DarkSurfaceContainerLow = Color(0xFF141820)
val DarkSurfaceContainer = Color(0xFF1A1F2A)
val DarkSurfaceContainerHigh = Color(0xFF232A38)
val DarkSurfaceContainerHighest = Color(0xFF2E3648)
val DarkInverseSurface = Color(0xFFE8EAED)
val DarkInverseOnSurface = Color(0xFF1A1F2A)
val DarkInversePrimary = Color(0xFF5B7300)
val DarkScrim = Color(0xFF000000)

// ── Reusable gradients ───────────────────────────────────────────────────────

object AiFitGradients {

    @Composable
    fun primaryGradient(): Brush {
        val primary = MaterialTheme.colorScheme.primary
        return Brush.linearGradient(
            colors = listOf(primary, primary.lighten(0.2f)),
            start = Offset.Zero,
            end = DiagonalGradientEnd,
        )
    }

    @Composable
    fun heroGradient(): Brush {
        val scheme = MaterialTheme.colorScheme
        return Brush.linearGradient(
            colors = listOf(
                scheme.primaryContainer,
                scheme.secondary.copy(alpha = 0.85f),
            ),
            start = Offset.Zero,
            end = DiagonalGradientEnd,
        )
    }

    @Composable
    fun cardSurfaceGradient(): Brush {
        val scheme = MaterialTheme.colorScheme
        return Brush.linearGradient(
            colors = listOf(
                scheme.surface,
                scheme.surfaceContainerHigh,
            ),
            start = Offset.Zero,
            end = DiagonalGradientEnd,
        )
    }

    @Composable
    fun achievementGradient(): Brush = Brush.linearGradient(
        colors = listOf(GradientGoldStart, GradientGoldEnd),
        start = Offset.Zero,
        end = DiagonalGradientEnd,
    )

    @Composable
    fun dangerGradient(): Brush {
        val scheme = MaterialTheme.colorScheme
        return Brush.linearGradient(
            colors = listOf(GradientDangerStart, scheme.errorContainer),
            start = Offset.Zero,
            end = DiagonalGradientEnd,
        )
    }
}

enum class MacroType {
    Protein,
    Carbs,
    Fat,
}

/** Colores y gradientes de macros alineados with [MacroRingChart]. */
object MacroColors {

    @Composable
    fun proteinColor(): Color = MaterialTheme.colorScheme.tertiary

    @Composable
    fun carbsColor(): Color = MaterialTheme.colorScheme.secondary

    @Composable
    fun fatColor(): Color = MaterialTheme.colorScheme.outline

    @Composable
    fun proteinBrush(): Brush {
        val scheme = MaterialTheme.colorScheme
        return Brush.horizontalGradient(
            colors = listOf(scheme.tertiaryContainer, scheme.tertiary),
        )
    }

    @Composable
    fun carbsBrush(): Brush {
        val scheme = MaterialTheme.colorScheme
        return Brush.horizontalGradient(
            colors = listOf(
                scheme.secondaryContainer,
                scheme.secondary,
            ),
        )
    }

    @Composable
    fun fatBrush(): Brush {
        val scheme = MaterialTheme.colorScheme
        return Brush.horizontalGradient(
            colors = listOf(
                scheme.surfaceVariant,
                scheme.outline,
            ),
        )
    }

    @Composable
    fun brushFor(macro: MacroType): Brush = when (macro) {
        MacroType.Protein -> proteinBrush()
        MacroType.Carbs -> carbsBrush()
        MacroType.Fat -> fatBrush()
    }

    @Composable
    fun colorFor(macro: MacroType): Color = when (macro) {
        MacroType.Protein -> proteinColor()
        MacroType.Carbs -> carbsColor()
        MacroType.Fat -> fatColor()
    }
}
