package com.jlsh.aifit.core.ui.components.display

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.*
import com.adamglin.phosphoricons.regular.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing

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
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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




