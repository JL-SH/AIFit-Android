package com.jlsh.aifit.core.ui.components.display

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitGradients
import com.jlsh.aifit.core.ui.theme.CardShape
import com.jlsh.aifit.core.ui.theme.aifitSubtleBorder

enum class CardVariant {
    Default,
    Hero,
    Subtle,
}

@Composable
fun AiFitCard(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Default,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val elevation = CardDefaults.cardElevation(
        defaultElevation = 0.dp,
        pressedElevation = 0.dp,
        hoveredElevation = 0.dp,
    )
    val shape = CardShape
    val backgroundBrush = cardBackgroundBrush(variant)
    val resolvedContainerColor = resolveContainerColor(variant, containerColor, backgroundBrush)
    val colors = CardDefaults.cardColors(containerColor = resolvedContainerColor)
    val cardModifier = modifier
        .fillMaxWidth()
        .clip(shape)
        .cardBorderModifier(variant, shape)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
        ) {
            CardBackground(brush = backgroundBrush, shape = shape, content = content)
        }
    } else {
        Card(
            modifier = cardModifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
        ) {
            CardBackground(brush = backgroundBrush, shape = shape, content = content)
        }
    }
}

@Composable
private fun resolveContainerColor(
    variant: CardVariant,
    containerColor: Color,
    backgroundBrush: Brush?,
): Color {
    if (backgroundBrush != null) return Color.Transparent
    return when (variant) {
        CardVariant.Subtle -> {
            val scheme = MaterialTheme.colorScheme
            if (isSystemInDarkTheme()) {
                scheme.surface.copy(alpha = 0.6f)
            } else {
                scheme.surfaceContainerLow
            }
        }
        CardVariant.Default,
        CardVariant.Hero,
        -> containerColor
    }
}

@Composable
private fun Modifier.cardBorderModifier(
    variant: CardVariant,
    shape: androidx.compose.ui.graphics.Shape,
): Modifier = when (variant) {
    CardVariant.Hero -> {
        val primaryBorder = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        border(1.dp, primaryBorder, shape)
    }
    CardVariant.Subtle -> {
        val subtleAlpha = if (isSystemInDarkTheme()) 0.15f else 0.08f
        val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = subtleAlpha)
        border(1.dp, borderColor, shape)
    }
    CardVariant.Default -> aifitSubtleBorder(shape = shape)
}

@Composable
private fun cardBackgroundBrush(variant: CardVariant): Brush? = when (variant) {
    CardVariant.Hero -> AiFitGradients.heroGradient()
    CardVariant.Subtle -> null
    CardVariant.Default -> null
}

@Composable
private fun CardBackground(
    brush: Brush?,
    shape: androidx.compose.ui.graphics.Shape,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (brush != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(brush),
        ) {
            Column(content = content)
        }
    } else {
        Column(content = content)
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark"
)
@Composable
private fun AiFitCardPreview() {
    AIFitTheme {
        AiFitCard {
            Text(
                text = "Plan de entrenamiento",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "4 días por semana",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark - Hero"
)
@Composable
private fun AiFitCardHeroPreview() {
    AIFitTheme {
        AiFitCard(variant = CardVariant.Hero) {
            Text(
                text = "Entreno de hoy",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light - Subtle"
)
@Composable
private fun AiFitCardSubtlePreview() {
    AIFitTheme(darkTheme = false) {
        AiFitCard(variant = CardVariant.Subtle) {
            Text(
                text = "Nutrición de hoy",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
