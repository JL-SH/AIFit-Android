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
 * Borde sutil para definir superficies en dark mode sin elevación/sombra.
 * En light mode usa alpha reducida para no competir con el fondo claro.
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
