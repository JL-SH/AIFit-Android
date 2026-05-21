package com.jlsh.aifit.core.ui.components.display

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.jlsh.aifit.core.ui.theme.AIFitTheme

/**
 * A single data point for [LineChartView].
 *
 * @property label Text identifying this point on the x-axis (e.g. a date or weekday).
 * @property value Numeric value plotted on the y-axis.
 */
data class ChartEntry(
    val label: String,
    val value: Float,
)

/**
 * Vico-powered line chart for rendering time-series data (e.g. body weight over time).
 *
 * Renders nothing when fewer than 2 [entries] are provided, since a single point
 * cannot form a meaningful line. The chart fills the available width and has a
 * fixed height of 200 dp.
 *
 * @param entries List of [ChartEntry] values to plot in order.
 * @param modifier Modifier applied to the [CartesianChartHost].
 */
@Composable
fun LineChartView(
    entries: List<ChartEntry>,
    modifier: Modifier = Modifier,
) {
    if (entries.size < 2) return

    val lineColor = MaterialTheme.colorScheme.primaryContainer

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(entries) {
        modelProducer.runTransaction {
            lineSeries {
                series(entries.map { it.value.toDouble() })
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(Fill(lineColor.toArgb())),
                    ),
                ),
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(),
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
    )
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark"
)
@Composable
private fun LineChartViewPreview() {
    AIFitTheme {
        LineChartView(
            entries = listOf(
                ChartEntry("Lun", 75.0f),
                ChartEntry("Mar", 74.5f),
                ChartEntry("Mié", 74.8f),
                ChartEntry("Jue", 74.2f),
                ChartEntry("Vie", 73.9f),
            ),
        )
    }
}
