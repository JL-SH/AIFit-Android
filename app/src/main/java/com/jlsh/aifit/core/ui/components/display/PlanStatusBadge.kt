package com.jlsh.aifit.core.ui.components.display

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlanStatusBadge(
    status: String,
    modifier: Modifier = Modifier,
) {
    val (backgroundColor, textColor) = statusColors(status)

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier,
    ) {
        Text(
            text = statusDisplayName(status),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.sp,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun statusDisplayName(status: String): String {
    return when (status.uppercase()) {
        "ACTIVE" -> stringResource(R.string.status_active)
        "COMPLETED" -> stringResource(R.string.status_completed)
        "DRAFT" -> stringResource(R.string.status_draft)
        "ARCHIVED" -> stringResource(R.string.status_archived)
        "PAUSED" -> stringResource(R.string.status_paused)
        else -> status.uppercase()
    }
}

@Composable
private fun statusColors(status: String): Pair<Color, Color> {
    return when (status.uppercase()) {
        "ACTIVE" -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
        )
        "COMPLETED" -> Pair(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
        )
        "DRAFT" -> Pair(
            MaterialTheme.colorScheme.surfaceContainerHigh,
            MaterialTheme.colorScheme.onSurfaceVariant,
        )
        "ARCHIVED" -> Pair(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
        )
        else -> Pair(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark - Active")
@Composable
private fun PlanStatusBadgeActivePreview() {
    AIFitTheme { PlanStatusBadge(status = "ACTIVE") }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark - Completed")
@Composable
private fun PlanStatusBadgeCompletedPreview() {
    AIFitTheme { PlanStatusBadge(status = "COMPLETED") }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark - Draft")
@Composable
private fun PlanStatusBadgeDraftPreview() {
    AIFitTheme { PlanStatusBadge(status = "DRAFT") }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark - Archived")
@Composable
private fun PlanStatusBadgeArchivedPreview() {
    AIFitTheme { PlanStatusBadge(status = "ARCHIVED") }
}
