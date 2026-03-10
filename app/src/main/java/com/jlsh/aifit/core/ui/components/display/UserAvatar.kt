package com.jlsh.aifit.core.ui.components.display

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jlsh.aifit.core.ui.theme.AIFitTheme

enum class AvatarSize(val size: Dp) {
    SMALL(32.dp),
    DEFAULT(40.dp),
    LARGE(64.dp),
}

@Composable
fun UserAvatar(
    name: String,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    size: AvatarSize = AvatarSize.DEFAULT,
) {
    Box(
        modifier = modifier
            .size(size.size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(size.size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = buildInitials(name),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

private fun buildInitials(name: String): String {
    val parts = name.trim().split("\\s+".toRegex())
    return when {
        parts.size >= 2 -> "${parts.first().first().uppercase()}${parts.last().first().uppercase()}"
        parts.isNotEmpty() && parts.first().isNotEmpty() -> parts.first().first().uppercase()
        else -> ""
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark - Initials"
)
@Composable
private fun UserAvatarInitialsPreview() {
    AIFitTheme {
        UserAvatar(
            name = "José López",
            size = AvatarSize.LARGE,
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark - Small"
)
@Composable
private fun UserAvatarSmallPreview() {
    AIFitTheme {
        UserAvatar(
            name = "Ana",
            size = AvatarSize.SMALL,
        )
    }
}

