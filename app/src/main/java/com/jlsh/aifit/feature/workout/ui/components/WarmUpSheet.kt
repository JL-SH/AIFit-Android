package com.jlsh.aifit.feature.workout.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.buttons.SecondaryButton
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.training.domain.model.WarmUpExercise
import com.jlsh.aifit.feature.training.domain.model.WarmUpProtocol

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarmUpSheet(
    protocol: WarmUpProtocol,
    onSkip: () -> Unit,
    onReady: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onSkip,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        WarmUpSheetContent(
            protocol = protocol,
            onSkip = onSkip,
            onReady = onReady,
        )
    }
}

@Composable
private fun WarmUpSheetContent(
    protocol: WarmUpProtocol,
    onSkip: () -> Unit,
    onReady: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AiFitSpacing.md)
            .navigationBarsPadding(),
    ) {
        Text(
            text = "Warm-Up Protocol",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(AiFitSpacing.md))

        protocol.exercises.forEachIndexed { index, exercise ->
            WarmUpExerciseItem(exercise = exercise)
            if (index < protocol.exercises.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = AiFitSpacing.xs),
                )
            }
        }

        Spacer(modifier = Modifier.height(AiFitSpacing.lg))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            SecondaryButton(
                text = "Skip",
                onClick = onSkip,
                modifier = Modifier.weight(1f),
            )
            PrimaryButton(
                text = "Ready, let's go",
                onClick = onReady,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(AiFitSpacing.md))
    }
}

@Composable
private fun WarmUpExerciseItem(exercise: WarmUpExercise) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AiFitSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
    ) {
        Text(
            text = exercise.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = exercise.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        val detailText = if (exercise.durationSeconds != null) {
            "${exercise.sets} × ${exercise.durationSeconds}s"
        } else {
            "${exercise.sets} × ${exercise.reps}"
        }

        Text(
            text = detailText,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primaryContainer,
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "WarmUpSheet Dark",
)
@Composable
private fun WarmUpSheetPreview() {
    AIFitTheme(darkTheme = true) {
        val fakeProtocol = WarmUpProtocol(
            trainingDayId = "d1",
            estimatedTotalLoad = 500.0,
            exercises = listOf(
                WarmUpExercise(
                    name = "Jumping Jacks",
                    description = "Full body warm-up to raise heart rate",
                    sets = 2,
                    reps = 20,
                    durationSeconds = null,
                ),
                WarmUpExercise(
                    name = "Arm Circles",
                    description = "Shoulder mobility and warm-up",
                    sets = 2,
                    reps = 15,
                    durationSeconds = null,
                ),
                WarmUpExercise(
                    name = "Plank Hold",
                    description = "Core activation",
                    sets = 2,
                    reps = 0,
                    durationSeconds = 30,
                ),
            ),
        )

        WarmUpSheetContent(
            protocol = fakeProtocol,
            onSkip = {},
            onReady = {},
        )
    }
}

