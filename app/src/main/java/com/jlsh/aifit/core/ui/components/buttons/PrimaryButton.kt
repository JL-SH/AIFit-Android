package com.jlsh.aifit.core.ui.components.buttons

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.FullShape

/**
 * Full-width primary CTA button styled with [MaterialTheme.colorScheme.primaryContainer].
 *
 * When [isLoading] is `true` the label is replaced by a [CircularProgressIndicator]
 * and interaction is disabled, preventing double-submissions. Elevation is kept
 * at 0 dp throughout all states to match the flat design system.
 *
 * @param text Label displayed inside the button.
 * @param onClick Lambda invoked when the button is tapped.
 * @param modifier Modifier applied to the outer [Button].
 * @param isLoading When `true`, hides the label and shows a spinner; also disables taps.
 * @param enabled When `false`, renders the button in its disabled visual state.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        enabled = enabled && !isLoading,
        shape = FullShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp,
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp,
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
private fun PrimaryButtonPreview() {
    AIFitTheme {
        PrimaryButton(
            text = "Iniciar sesión",
            onClick = {},
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark - Loading"
)
@Composable
private fun PrimaryButtonLoadingPreview() {
    AIFitTheme {
        PrimaryButton(
            text = "Iniciar sesión",
            onClick = {},
            isLoading = true,
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark - Disabled"
)
@Composable
private fun PrimaryButtonDisabledPreview() {
    AIFitTheme {
        PrimaryButton(
            text = "Iniciar sesión",
            onClick = {},
            enabled = false,
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light"
)
@Composable
private fun PrimaryButtonLightPreview() {
    AIFitTheme(darkTheme = false) {
        PrimaryButton(
            text = "Registrar comida",
            onClick = {},
        )
    }
}
