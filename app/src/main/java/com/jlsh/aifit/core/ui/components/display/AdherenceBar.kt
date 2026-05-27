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

@Composable
fun AdherenceBar(
    percentage: Float,
    modifier: Modifier = Modifier,
) {
    val fillBrush = adherenceBrush(percentage)
    val fillColor = adherenceColor(percentage)
    val progress = (percentage / 100f).coerceIn(0f, 1f)
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
                text = "Adherencia",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${percentage.toInt()}%",
                style = MaterialTheme.typography.titleSmall,
                color = fillColor,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
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
private fun adherenceBrush(percentage: Float): Brush {
    val scheme = MaterialTheme.colorScheme
    return when {
        percentage >= 70f -> Brush.horizontalGradient(
            colors = listOf(scheme.primaryContainer, scheme.primary),
        )
        percentage >= 40f -> Brush.horizontalGradient(
            colors = listOf(scheme.tertiaryContainer, scheme.tertiary),
        )
        else -> Brush.horizontalGradient(
            colors = listOf(scheme.errorContainer, scheme.error),
        )
    }
}

@Composable
private fun adherenceColor(percentage: Float): Color {
    val scheme = MaterialTheme.colorScheme
    return when {
        percentage >= 70f -> scheme.primary
        percentage >= 40f -> scheme.tertiary
        else -> scheme.error
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark - High"
)
@Composable
private fun AdherenceBarHighPreview() {
    AIFitTheme {
        AdherenceBar(percentage = 85f)
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark - Medium"
)
@Composable
private fun AdherenceBarMediumPreview() {
    AIFitTheme {
        AdherenceBar(percentage = 50f)
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark - Low"
)
@Composable
private fun AdherenceBarLowPreview() {
    AIFitTheme {
        AdherenceBar(percentage = 20f)
    }
}
