package com.jlsh.aifit.core.ui.components.display

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jlsh.aifit.core.ui.theme.AIFitTheme

data class MacroRingData(
    val currentCalories: Float,
    val targetCalories: Float,
    val currentProtein: Float,
    val targetProtein: Float,
    val currentCarbs: Float,
    val targetCarbs: Float,
    val currentFat: Float,
    val targetFat: Float,
)

@Composable
fun MacroRingChart(
    data: MacroRingData,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 12.dp,
) {
    val proteinColor = MaterialTheme.colorScheme.tertiary
    val carbsColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
    val fatColor = MaterialTheme.colorScheme.outline
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    val totalTarget = data.targetCalories
    val totalCurrent = data.currentCalories

    val strokePx = with(LocalDensity.current) { strokeWidth.toPx() }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = this.size.minDimension
            val arcSize = Size(canvasSize - strokePx, canvasSize - strokePx)
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            // Draw track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            // Calculate sweep angles based on macro contribution to total calories
            // Protein: 4 kcal/g, Carbs: 4 kcal/g, Fat: 9 kcal/g
            val proteinKcal = data.currentProtein * 4f
            val carbsKcal = data.currentCarbs * 4f
            val fatKcal = data.currentFat * 9f
            val totalMacroKcal = proteinKcal + carbsKcal + fatKcal

            if (totalMacroKcal > 0f && totalTarget > 0f) {
                val totalSweep = (totalCurrent / totalTarget).coerceAtMost(1f) * 360f

                val proteinFraction = proteinKcal / totalMacroKcal
                val carbsFraction = carbsKcal / totalMacroKcal
                val fatFraction = fatKcal / totalMacroKcal

                var startAngle = -90f

                // Protein segment
                val proteinSweep = totalSweep * proteinFraction
                drawArc(
                    color = proteinColor,
                    startAngle = startAngle,
                    sweepAngle = proteinSweep.coerceAtLeast(0f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Butt),
                )
                startAngle += proteinSweep

                // Carbs segment
                val carbsSweep = totalSweep * carbsFraction
                drawArc(
                    color = carbsColor,
                    startAngle = startAngle,
                    sweepAngle = carbsSweep.coerceAtLeast(0f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Butt),
                )
                startAngle += carbsSweep

                // Fat segment
                val fatSweep = totalSweep * fatFraction
                drawArc(
                    color = fatColor,
                    startAngle = startAngle,
                    sweepAngle = fatSweep.coerceAtLeast(0f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Butt),
                )
            }
        }

        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${data.currentCalories.toInt()}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "kcal",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark"
)
@Composable
private fun MacroRingChartPreview() {
    AIFitTheme {
        MacroRingChart(
            data = MacroRingData(
                currentCalories = 1850f,
                targetCalories = 2500f,
                currentProtein = 120f,
                targetProtein = 180f,
                currentCarbs = 200f,
                targetCarbs = 300f,
                currentFat = 60f,
                targetFat = 80f,
            ),
        )
    }
}


