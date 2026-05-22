package com.jlsh.aifit.core.ui.components.feedback

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.buttons.SecondaryButton
import com.jlsh.aifit.core.ui.theme.AIFitTheme

/**
 * Full-screen error state composed of an error icon, a human-readable message
 * and a [SecondaryButton] that lets the user retry the failed operation.
 *
 * Fills the available space with [MaterialTheme.colorScheme.background] and centres
 * its contents vertically and horizontally. Tagged with `"error_screen"` for UI tests.
 *
 * @param message Human-readable description of the error to display.
 * @param onRetry Lambda invoked when the user taps the retry button.
 * @param modifier Modifier applied to the outer [Box].
 */
@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("error_screen"),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = PhosphorIcons.Regular.XCircle,
                contentDescription = stringResource(R.string.component_error_icon_cd),
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.error,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 4,
                modifier = Modifier.widthIn(max = 240.dp),
            )
            SecondaryButton(
                text = stringResource(R.string.common_retry),
                onClick = onRetry,
                modifier = Modifier.width(160.dp),
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark"
)
@Composable
private fun ErrorScreenPreview() {
    AIFitTheme(darkTheme = true) {
        ErrorScreen(
            message = "Sin conexión. Comprueba tu internet.",
            onRetry = {},
        )
    }
}
