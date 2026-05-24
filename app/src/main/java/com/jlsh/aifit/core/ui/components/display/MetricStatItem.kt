package com.jlsh.aifit.core.ui.components.display

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitTextStyles

@Composable
fun MetricStatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.primary,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = AiFitTextStyles.metricDisplay,
            color = valueColor,
        )
        Text(
            text = label.uppercase(),
            style = AiFitTextStyles.metricLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun MetricStatItem(
    targetInt: Int,
    label: String,
    modifier: Modifier = Modifier,
    suffix: String = "",
    prefix: String = "",
    valueColor: Color = MaterialTheme.colorScheme.primary,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedMetricText(
            target = targetInt,
            suffix = suffix,
            prefix = prefix,
            style = AiFitTextStyles.metricDisplay,
            color = valueColor,
        )
        Text(
            text = label.uppercase(),
            style = AiFitTextStyles.metricLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun MetricStatItem(
    targetFloat: Float,
    label: String,
    modifier: Modifier = Modifier,
    suffix: String = "",
    prefix: String = "",
    formatter: (Float) -> String = { "%.1f".format(it) },
    valueColor: Color = MaterialTheme.colorScheme.primary,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedMetricText(
            target = targetFloat,
            suffix = suffix,
            prefix = prefix,
            formatter = formatter,
            style = AiFitTextStyles.metricDisplay,
            color = valueColor,
        )
        Text(
            text = label.uppercase(),
            style = AiFitTextStyles.metricLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark",
)
@Composable
private fun MetricStatItemPreview() {
    AIFitTheme {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        ) {
            MetricStatItem(targetFloat = 76.2f, label = "Actual")
            MetricStatItem(
                targetFloat = 2.3f,
                label = "Cambio",
                prefix = "+",
                valueColor = MaterialTheme.colorScheme.primaryContainer,
            )
        }
    }
}
