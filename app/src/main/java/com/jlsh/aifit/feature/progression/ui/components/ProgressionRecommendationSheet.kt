package com.jlsh.aifit.feature.progression.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.feedback.InlineLoadingIndicator
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.progression.ui.RecommendationState
import com.jlsh.aifit.feature.user.ui.toStringRes

/**
 * Hoja inferior modal con la recomendación de progresión de un ejercicio.
 *
 * Muestra indicador de carga, detalle de éxito (nombre, tipo, carga actual → sugerida,
 * repeticiones, justificación y confianza) o mensaje de error con botón de reintento.
 *
 * @param state Estado actual de la recomendación ([RecommendationState]).
 * @param onDismiss Callback al cerrar la hoja (deslizar o fuera del área).
 * @param onRetry Reintenta la carga cuando [state] es [RecommendationState.Error].
 * @param sheetState Estado de animación de la [ModalBottomSheet]; por defecto expandida.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressionRecommendationSheet(
    state: RecommendationState,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
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
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            when (state) {
                is RecommendationState.Loading -> {
                    InlineLoadingIndicator(
                        message = stringResource(R.string.progression_loading),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AiFitSpacing.lg),
                    )
                }

                is RecommendationState.Success -> {
                    val rec = state.data

                    Text(
                        text = rec.exerciseName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    // Type badge
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            text = stringResource(rec.type.toStringRes()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }

                    // Load progression
                    if (rec.currentLoad != null && rec.suggestedLoad != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.progression_current_load_label),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = stringResource(R.string.progression_load_kg, rec.currentLoad.toString()),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            Text(
                                text = "→",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primaryContainer,
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = stringResource(R.string.progression_suggested_load_label),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = stringResource(R.string.progression_load_kg, rec.suggestedLoad.toString()),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                )
                            }
                        }
                    }

                    // Reps
                    if (rec.suggestedRepsMin > 0 || rec.suggestedRepsMax > 0) {
                        Text(
                            text = stringResource(R.string.progression_reps_range, rec.suggestedRepsMin, rec.suggestedRepsMax),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    Spacer(modifier = Modifier.height(AiFitSpacing.xs))

                    // Rationale
                    Text(
                        text = rec.rationale,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    // Confidence + sessions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(R.string.progression_confidence, (rec.confidence * 100).toInt()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = stringResource(R.string.progression_based_on_sessions, rec.basedOnSessions),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                is RecommendationState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AiFitSpacing.lg),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                        TextButton(onClick = onRetry) {
                            Text(stringResource(R.string.progression_retry))
                        }
                    }
                }

                is RecommendationState.Idle -> Unit
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark")
@Composable
private fun ProgressionSheetPreview() {
    AIFitTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(AiFitSpacing.md)) {
            Text(
                text = "Bench Press",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "60.0 kg → 62.5 kg",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primaryContainer,
            )
            Text(
                text = "Increase weight by 2.5kg based on consistent performance.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
