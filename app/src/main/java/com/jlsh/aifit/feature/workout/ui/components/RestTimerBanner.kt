package com.jlsh.aifit.feature.workout.ui.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing

@Composable
fun RestTimerBanner(
    seconds: Int?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (seconds == null) return

    val haptic = LocalHapticFeedback.current
    val isComplete = seconds == 0

    if (isComplete) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (isComplete) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        animationSpec = tween(durationMillis = 300),
        label = "timerBgColor",
    )

    val textColor = if (isComplete) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    val minutes = seconds / 60
    val secs = seconds % 60
    val timeText = if (isComplete) "Rest complete!" else "%d:%02d".format(minutes, secs)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(backgroundColor)
            .clickable { onDismiss() }
            .padding(horizontal = AiFitSpacing.md, vertical = AiFitSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = timeText,
            style = MaterialTheme.typography.titleLarge,
            color = textColor,
        )
        Text(
            text = "Cerrar",
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "RestTimerBanner Counting Dark",
)
@Composable
private fun RestTimerBannerCountingPreview() {
    AIFitTheme(darkTheme = true) {
        RestTimerBanner(
            seconds = 92,
            onDismiss = {},
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "RestTimerBanner Complete Dark",
)
@Composable
private fun RestTimerBannerCompletePreview() {
    AIFitTheme(darkTheme = true) {
        RestTimerBanner(
            seconds = 0,
            onDismiss = {},
        )
    }
}

// U-12/U-13 compliance fix — light mode preview
@Preview(
    showBackground = true,
    name = "RestTimerBanner Counting Light",
)
@Composable
private fun RestTimerBannerCountingLightPreview() {
    AIFitTheme(darkTheme = false) {
        RestTimerBanner(
            seconds = 92,
            onDismiss = {},
        )
    }
}

// U-12/U-13 compliance fix — light mode preview
@Preview(
    showBackground = true,
    name = "RestTimerBanner Complete Light",
)
@Composable
private fun RestTimerBannerCompleteLightPreview() {
    AIFitTheme(darkTheme = false) {
        RestTimerBanner(
            seconds = 0,
            onDismiss = {},
        )
    }
}

