package com.jlsh.aifit.core.ui.components.feedback

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.buttons.DestructiveButton
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.theme.AIFitTheme

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = stringResource(R.string.common_confirm),
    dismissText: String = stringResource(R.string.common_cancel),
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            PrimaryButton(
                text = confirmText,
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        dismissButton = {
            DestructiveButton(
                text = dismissText,
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    )
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark"
)
@Composable
private fun ConfirmationDialogPreview() {
    AIFitTheme(darkTheme = true) {
        ConfirmationDialog(
            title = "Eliminar plan",
            message = "Esta acción no se puede deshacer.",
            onConfirm = {},
            onDismiss = {},
        )
    }
}
