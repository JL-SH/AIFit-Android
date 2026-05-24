package com.jlsh.aifit.core.ui.components.display

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitGradients
import com.jlsh.aifit.core.ui.theme.aifitSubtleBorder

private val BadgeShape = RoundedCornerShape(6.dp)

/**
 * Small badge that renders a localised label for a plan status value.
 */
@Composable
fun PlanStatusBadge(
    status: String,
    modifier: Modifier = Modifier,
) {
    val (backgroundBrush, backgroundColor, textColor) = statusStyle(status)

    Box(
        modifier = modifier
            .clip(BadgeShape)
            .then(
                if (backgroundBrush != null) {
                    Modifier.background(backgroundBrush)
                } else {
                    Modifier
                        .background(backgroundColor)
                        .aifitSubtleBorder(shape = BadgeShape)
                },
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = statusDisplayName(status),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.sp,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Visible,
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
        else -> status
    }
}

@Composable
private fun statusStyle(status: String): Triple<Brush?, Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when (status.uppercase()) {
        "ACTIVE" -> Triple(
            AiFitGradients.primaryGradient(),
            Color.Transparent,
            scheme.onPrimaryContainer,
        )
        "COMPLETED" -> Triple(
            AiFitGradients.achievementGradient(),
            Color.Transparent,
            Color(0xFF1A1200),
        )
        "DRAFT" -> Triple(
            null,
            scheme.surfaceContainerHigh,
            scheme.onSurfaceVariant,
        )
        "ARCHIVED",
        "PAUSED",
        -> Triple(
            null,
            scheme.surfaceVariant,
            scheme.onSurfaceVariant,
        )
        else -> Triple(
            null,
            scheme.surfaceVariant,
            scheme.onSurfaceVariant,
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
