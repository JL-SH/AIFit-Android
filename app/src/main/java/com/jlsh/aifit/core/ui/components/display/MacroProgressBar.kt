package com.jlsh.aifit.core.ui.components.display

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.core.ui.theme.ChipShape
import com.jlsh.aifit.core.ui.theme.MacroColors
import com.jlsh.aifit.core.ui.theme.MacroType

@Composable
fun MacroProgressBar(
    name: String,
    current: Float,
    target: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    macro: MacroType? = null,
) {
    val isOverTarget = current > target
    val fillBrush = resolveFillBrush(macro, color, isOverTarget)
    val labelColor = if (isOverTarget) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val progress = if (target > 0f) (current / target).coerceIn(0f, 1f) else 0f
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${current.toInt()}g / ${target.toInt()}g",
                style = MaterialTheme.typography.labelMedium,
                color = labelColor,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(ChipShape)
                .background(trackColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(ChipShape)
                    .background(fillBrush),
            )
        }
    }
}

@Composable
private fun resolveFillBrush(
    macro: MacroType?,
    fallbackColor: Color,
    isOverTarget: Boolean,
): Brush {
    if (isOverTarget) {
        val scheme = MaterialTheme.colorScheme
        return Brush.horizontalGradient(
            colors = listOf(scheme.errorContainer, scheme.error),
        )
    }
    if (macro != null) {
        return MacroColors.brushFor(macro)
    }
    return Brush.horizontalGradient(
        colors = listOf(fallbackColor, fallbackColor.copy(alpha = 0.7f)),
    )
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark"
)
@Composable
private fun MacroProgressBarPreview() {
    AIFitTheme {
        MacroProgressBar(
            name = "Proteína",
            current = 120f,
            target = 180f,
            macro = MacroType.Protein,
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark - Over Target"
)
@Composable
private fun MacroProgressBarOverPreview() {
    AIFitTheme {
        MacroProgressBar(
            name = "Carbohidratos",
            current = 320f,
            target = 250f,
            macro = MacroType.Carbs,
        )
    }
}
