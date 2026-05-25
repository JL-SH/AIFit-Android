package com.jlsh.aifit.core.ui.theme

import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Subtle border to define surfaces in dark mode without elevation/shadow.
 * In light mode uses reduced alpha so it does not compete with the light background.
 */
@Composable
fun Modifier.aifitSubtleBorder(
    shape: Shape = CardShape,
    width: Dp = 1.dp,
    force: Boolean = false,
): Modifier {
    val darkTheme = isSystemInDarkTheme()
    if (!darkTheme && !force) {
        val lightAlpha = 0.08f
        val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = lightAlpha)
        return border(width, borderColor, shape)
    }
    val alpha = if (darkTheme) 0.15f else 0.08f
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = alpha)
    return border(width, borderColor, shape)
}
