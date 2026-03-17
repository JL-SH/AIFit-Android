package com.jlsh.aifit.feature.workout.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jlsh.aifit.core.ui.components.feedback.InlineLoadingIndicator
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.training.domain.model.ExerciseSubstitution
import com.jlsh.aifit.feature.training.domain.model.MuscleGroup
import com.jlsh.aifit.feature.workout.ui.state.SubstitutionLoadState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstitutionSheet(
    state: SubstitutionLoadState,
    onSelect: (ExerciseSubstitution) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        SubstitutionSheetContent(
            state = state,
            onSelect = onSelect,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun SubstitutionSheetContent(
    state: SubstitutionLoadState,
    onSelect: (ExerciseSubstitution) -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AiFitSpacing.md)
            .navigationBarsPadding(),
    ) {
        Text(
            text = "Exercise Alternatives",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(AiFitSpacing.md))

        when (state) {
            is SubstitutionLoadState.Idle,
            is SubstitutionLoadState.Loading -> {
                InlineLoadingIndicator(
                    message = "Finding alternatives...",
                    modifier = Modifier.padding(vertical = AiFitSpacing.lg),
                )
            }

            is SubstitutionLoadState.Success -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
                ) {
                    items(state.substitutions, key = { it.name }) { substitution ->
                        SubstitutionItem(
                            substitution = substitution,
                            onClick = { onSelect(substitution) },
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 0.5.dp,
                        )
                    }
                }
            }

            is SubstitutionLoadState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AiFitSpacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    TextButton(onClick = onDismiss) {
                        Text(text = "Retry")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(AiFitSpacing.md))
    }
}

@Composable
private fun SubstitutionItem(
    substitution: ExerciseSubstitution,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = AiFitSpacing.sm),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
        ) {
            Text(
                text = substitution.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(6.dp),
            ) {
                Text(
                    text = substitution.primaryMuscle.name
                        .replace("_", " ")
                        .lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = AiFitSpacing.sm, vertical = AiFitSpacing.xs),
                )
            }
            Text(
                text = substitution.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "SubstitutionSheet Success Dark",
)
@Composable
private fun SubstitutionSheetSuccessPreview() {
    AIFitTheme(darkTheme = true) {
        SubstitutionSheetContent(
            state = SubstitutionLoadState.Success(
                substitutions = listOf(
                    ExerciseSubstitution(
                        name = "Dumbbell Bench Press",
                        primaryMuscle = MuscleGroup.CHEST,
                        movementPattern = "Horizontal Push",
                        description = "Similar pressing motion with greater range of motion and unilateral stability demands.",
                    ),
                    ExerciseSubstitution(
                        name = "Machine Chest Press",
                        primaryMuscle = MuscleGroup.CHEST,
                        movementPattern = "Horizontal Push",
                        description = "Guided pressing movement, ideal when stability is limited or for isolation focus.",
                    ),
                ),
            ),
            onSelect = {},
            onDismiss = {},
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "SubstitutionSheet Loading Dark",
)
@Composable
private fun SubstitutionSheetLoadingPreview() {
    AIFitTheme(darkTheme = true) {
        SubstitutionSheetContent(
            state = SubstitutionLoadState.Loading,
            onSelect = {},
            onDismiss = {},
        )
    }
}

// U-12/U-13 compliance fix — light mode preview
@Preview(
    showBackground = true,
    name = "SubstitutionSheet Light",
)
@Composable
private fun SubstitutionSheetLightPreview() {
    AIFitTheme(darkTheme = false) {
        SubstitutionSheetContent(
            state = SubstitutionLoadState.Success(
                substitutions = listOf(
                    ExerciseSubstitution(
                        name = "Dumbbell Bench Press",
                        primaryMuscle = MuscleGroup.CHEST,
                        movementPattern = "Horizontal Push",
                        description = "Similar pressing motion with greater range of motion.",
                    ),
                ),
            ),
            onSelect = {},
            onDismiss = {},
        )
    }
}

