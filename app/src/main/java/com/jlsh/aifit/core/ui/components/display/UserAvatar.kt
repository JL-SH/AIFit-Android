package com.jlsh.aifit.core.ui.components.display

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jlsh.aifit.core.ui.theme.AIFitTheme

/**
 * Available size presets for [UserAvatar].
 *
 * @property size Diameter of the avatar circle in density-independent pixels.
 */
enum class AvatarSize(val size: Dp) {
    SMALL(32.dp),
    DEFAULT(40.dp),
    LARGE(64.dp),
}

/**
 * Circular avatar that displays a remote image or falls back to the user's initials.
 *
 * Coil loads [imageUrl] asynchronously. If the load fails for any reason (network
 * error, bad URL, timeout), the component falls back to rendering initials derived
 * from [name] on a [MaterialTheme.colorScheme.secondaryContainer] background.
 *
 * @param name User's full name, used both as the content description for the image
 *   and to derive the 1–2 character initials shown as fallback.
 * @param modifier Modifier applied to the outer [Box].
 * @param imageUrl Optional remote image URL. When `null` or blank, initials are shown.
 * @param size Controls the diameter of the avatar circle; see [AvatarSize].
 */
@Composable
fun UserAvatar(
    name: String,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    size: AvatarSize = AvatarSize.DEFAULT,
) {
    var coilFailed by remember(imageUrl) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(size.size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (!imageUrl.isNullOrBlank() && !coilFailed) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(size.size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                onError = { coilFailed = true },
            )
        } else {
            Text(
                text = buildInitials(name),
                style = if (size == AvatarSize.LARGE) MaterialTheme.typography.titleMedium
                else MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
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
