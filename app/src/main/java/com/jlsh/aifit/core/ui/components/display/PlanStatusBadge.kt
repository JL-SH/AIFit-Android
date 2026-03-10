package com.jlsh.aifit.core.ui.components.display

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing

@Composable
fun PlanStatusBadge(
    status: String,
    modifier: Modifier = Modifier,
) {
    val (backgroundColor, textColor) = statusColors(status)

    Text(
        text = status.uppercase(),
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(backgroundColor)
            .padding(horizontal = AiFitSpacing.sm, vertical = AiFitSpacing.xs),
        style = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 1.5.sp,
        ),
        color = textColor,
    )
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

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark - Active"
)
@Composable
private fun PlanStatusBadgeActivePreview() {
    AIFitTheme {
        PlanStatusBadge(status = "ACTIVE")
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark - Completed"
)
@Composable
private fun PlanStatusBadgeCompletedPreview() {
    AIFitTheme {
        PlanStatusBadge(status = "COMPLETED")
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark - Draft"
)
@Composable
private fun PlanStatusBadgeDraftPreview() {
    AIFitTheme {
        PlanStatusBadge(status = "DRAFT")
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark - Archived"
)
@Composable
private fun PlanStatusBadgeArchivedPreview() {
    AIFitTheme {
        PlanStatusBadge(status = "ARCHIVED")
    }
}



