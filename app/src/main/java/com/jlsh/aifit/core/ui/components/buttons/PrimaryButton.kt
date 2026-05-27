package com.jlsh.aifit.core.ui.components.buttons

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitGradients
import com.jlsh.aifit.core.ui.theme.ButtonShape

/**
 * Full-width primary CTA button styled with [MaterialTheme.colorScheme.primaryContainer],
 * or an optional gradient when [useGradient] is true.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    useGradient: Boolean = false,
) {
    if (useGradient) {
        GradientPrimaryButton(
            text = text,
            onClick = onClick,
            modifier = modifier,
            isLoading = isLoading,
            enabled = enabled,
        )
    } else {
        SolidPrimaryButton(
            text = text,
            onClick = onClick,
            modifier = modifier,
            isLoading = isLoading,
            enabled = enabled,
        )
    }
}

@Composable
private fun SolidPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    isLoading: Boolean,
    enabled: Boolean,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        enabled = enabled && !isLoading,
        shape = ButtonShape,
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
                text = text.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp,
            )
        }
    }
}

@Composable
private fun GradientPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    isLoading: Boolean,
    enabled: Boolean,
) {
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    val interactionSource = remember { MutableInteractionSource() }
    val isInteractive = enabled && !isLoading

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(ButtonShape)
            .background(AiFitGradients.primaryGradient())
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = contentColor),
                enabled = isInteractive,
                role = Role.Button,
                onClick = onClick,
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
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
    name = "Dark - Gradient"
)
@Composable
private fun PrimaryButtonGradientPreview() {
    AIFitTheme {
        PrimaryButton(
            text = "Iniciar sesión",
            onClick = {},
            useGradient = true,
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
