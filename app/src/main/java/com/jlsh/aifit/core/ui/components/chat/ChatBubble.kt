package com.jlsh.aifit.core.ui.components.chat

import android.content.res.Configuration
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun ChatBubble(
    content: String,
    isUser: Boolean,
    timestamp: String,
    modifier: Modifier = Modifier,
    isMarkdown: Boolean = false,
    imageBase64: String? = null,
) {
    val bubbleShape = if (isUser) {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 6.dp,
        )
    } else {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 6.dp,
            bottomEnd = 16.dp,
        )
    }

    val backgroundColor = if (isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val textColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val alignment = if (isUser) Alignment.End else Alignment.Start

    // Decode Base64 image bytes for Coil
    val imageBytes = remember(imageBase64) {
        imageBase64?.let {
            runCatching { Base64.decode(it, Base64.NO_WRAP) }.getOrNull()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = alignment,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .background(backgroundColor)
                .padding(horizontal = AiFitSpacing.sm + AiFitSpacing.xs, vertical = AiFitSpacing.sm),
        ) {
            Column {
                // Show image preview if present
                if (imageBytes != null) {
                    AsyncImage(
                        model = imageBytes,
                        contentDescription = "Imagen adjunta",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .widthIn(max = 260.dp)
                            .height(180.dp)
                            .clip(MaterialTheme.shapes.small),
                    )
                    if (content.isNotBlank() && content != "📷 Imagen adjunta") {
                        Spacer(modifier = Modifier.height(AiFitSpacing.xs))
                    }
                }

                // Show text content (skip the placeholder text when there's an image and no real text)
                val showText = content.isNotBlank() &&
                    !(imageBytes != null && content == "📷 Imagen adjunta")
                if (showText) {
                    if (isMarkdown && !isUser) {
                        MarkdownText(
                            markdown = content,
                            style = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                        )
                    } else {
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor,
                        )
                    }
                }
            }
        }
        Text(
            text = timestamp,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = AiFitSpacing.xs),
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark - User"
)
@Composable
private fun ChatBubbleUserPreview() {
    AIFitTheme {
        ChatBubble(
            content = "¿Puedes generar un plan de entrenamiento para mí?",
            isUser = true,
            timestamp = "10:30",
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark - Assistant Markdown"
)
@Composable
private fun ChatBubbleAssistantPreview() {
    AIFitTheme {
        ChatBubble(
            content = "¡Claro! Voy a crear un **plan personalizado** basado en tu perfil.\n\n- Frecuencia: 4 días\n- Enfoque: Hipertrofia",
            isUser = false,
            timestamp = "10:31",
            isMarkdown = true,
        )
    }
}
