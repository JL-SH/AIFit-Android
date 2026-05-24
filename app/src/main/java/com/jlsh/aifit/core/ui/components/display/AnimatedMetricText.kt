package com.jlsh.aifit.core.ui.components.display

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitMotion
import com.jlsh.aifit.core.ui.theme.AiFitTextStyles

@Composable
fun AnimatedMetricText(
    target: Int,
    modifier: Modifier = Modifier,
    suffix: String = "",
    prefix: String = "",
    style: TextStyle = AiFitTextStyles.metricDisplay,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val animatedValue by animateIntAsState(
        targetValue = target,
        animationSpec = AiFitMotion.metricCountUpTween(),
        label = "metric_int",
    )
    Text(
        text = "$prefix$animatedValue$suffix",
        style = style,
        color = color,
        modifier = modifier,
    )
}

@Composable
fun AnimatedMetricText(
    target: Float,
    modifier: Modifier = Modifier,
    suffix: String = "",
    prefix: String = "",
    formatter: (Float) -> String = { "%.1f".format(it) },
    style: TextStyle = AiFitTextStyles.metricDisplay,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val animatedValue by animateFloatAsState(
        targetValue = target,
        animationSpec = AiFitMotion.metricCountUpTween(),
        label = "metric_float",
    )
    Text(
        text = "$prefix${formatter(animatedValue)}$suffix",
        style = style,
        color = color,
        modifier = modifier,
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark")
@Composable
private fun AnimatedMetricTextPreview() {
    AIFitTheme {
        AnimatedMetricText(target = 1847, suffix = " kcal")
    }
}
