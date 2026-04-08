package com.jlsh.aifit.feature.progression.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.theme.AiFitSpacing

private const val MIN_SESSIONS_REQUIRED = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressionIntroSheet(
    sessionCount: Int?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val count = sessionCount ?: 0
    val hasEnoughSessions = count >= MIN_SESSIONS_REQUIRED

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AiFitSpacing.md)
                .padding(bottom = AiFitSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp),
            )

            Text(
                text = "Progresión de carga",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            if (hasEnoughSessions) {
                Text(
                    text = "La progresión de carga consiste en aumentar gradualmente el peso, las repeticiones o el volumen de tus ejercicios para seguir mejorando.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = "Nuestra IA analizará tu historial de entrenamiento para recomendarte cuánto peso añadir en este ejercicio.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(AiFitSpacing.sm))

                PrimaryButton(
                    text = "Sí, recomiéndame una progresión",
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Text(
                    text = "Necesitas al menos $MIN_SESSIONS_REQUIRED sesiones registradas para obtener recomendaciones. Tienes $count sesiones.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(AiFitSpacing.sm))

                PrimaryButton(
                    text = "Entendido",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

