package com.jlsh.aifit.core.ui.components.display

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.fill.Fire
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitGradients
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.core.ui.theme.GradientGoldStart

private val BadgeShape = RoundedCornerShape(6.dp)

enum class StreakStatus {
    ACTIVE,
    FROZEN,
    BROKEN,
}

@Composable
fun StreakBadge(
    count: Int,
    label: String,
    status: StreakStatus,
    modifier: Modifier = Modifier,
) {
    when (status) {
        StreakStatus.ACTIVE -> ActiveStreakBadge(
            count = count,
            label = label,
            modifier = modifier,
        )
        StreakStatus.FROZEN,
        StreakStatus.BROKEN,
        -> InactiveStreakBadge(
            count = count,
            label = label,
            status = status,
            modifier = modifier,
        )
    }
}

@Composable
private fun ActiveStreakBadge(
    count: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    val contentColor = Color(0xFF1A1200)
    val isHotStreak = count >= 7
    val fireTint = if (isHotStreak) {
        val infiniteTransition = rememberInfiniteTransition(label = "hotStreak")
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.55f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "firePulse",
        )
        GradientGoldStart.copy(alpha = pulseAlpha)
    } else {
        contentColor
    }

    Row(
        modifier = modifier
            .clip(BadgeShape)
            .background(AiFitGradients.achievementGradient())
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = PhosphorIcons.Fill.Fire,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = fireTint,
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleSmall,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Visible,
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.75f),
            maxLines = 1,
            overflow = TextOverflow.Visible,
        )
    }
}

@Composable
private fun InactiveStreakBadge(
    count: Int,
    label: String,
    status: StreakStatus,
    modifier: Modifier = Modifier,
) {
    val accentColor = streakColor(status)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = PhosphorIcons.Fill.Fire,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = accentColor,
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleSmall,
            color = accentColor,
            maxLines = 1,
            overflow = TextOverflow.Visible,
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Visible,
        )
    }
}

@Composable
private fun streakColor(status: StreakStatus): Color {
    return when (status) {
        StreakStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer
        StreakStatus.FROZEN -> MaterialTheme.colorScheme.onSurfaceVariant
        StreakStatus.BROKEN -> MaterialTheme.colorScheme.error
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark - Active"
)
@Composable
private fun StreakBadgeActivePreview() {
    AIFitTheme {
        StreakBadge(
            count = 12,
            label = "Day Streak",
            status = StreakStatus.ACTIVE,
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark - Hot streak"
)
@Composable
private fun StreakBadgeHotPreview() {
    AIFitTheme {
        StreakBadge(
            count = 7,
            label = "Entrenamiento",
            status = StreakStatus.ACTIVE,
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark - Frozen"
)
@Composable
private fun StreakBadgeFrozenPreview() {
    AIFitTheme {
        StreakBadge(
            count = 5,
            label = "Day Streak",
            status = StreakStatus.FROZEN,
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark - Broken"
)
@Composable
private fun StreakBadgeBrokenPreview() {
    AIFitTheme {
        StreakBadge(
            count = 0,
            label = "Day Streak",
            status = StreakStatus.BROKEN,
        )
    }
}
