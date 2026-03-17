package com.jlsh.aifit.feature.workout.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jlsh.aifit.core.ui.components.layout.ExpandableSection
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.training.domain.model.MuscleGroup
import java.text.NumberFormat
import java.util.Locale

@Composable
fun VolumePanelSection(
    volumeByMuscleGroup: Map<MuscleGroup, Double>,
    modifier: Modifier = Modifier,
) {
    if (volumeByMuscleGroup.isEmpty()) return

    val formatter = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 0
    }

    ExpandableSection(
        title = "Session Volume",
        modifier = modifier,
        initiallyExpanded = false,
    ) {
        volumeByMuscleGroup.forEach { (muscleGroup, volume) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AiFitSpacing.xs, horizontal = AiFitSpacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = muscleGroup.name
                        .replace("_", " ")
                        .lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${formatter.format(volume)} kg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "VolumePanelSection Dark",
)
@Composable
private fun VolumePanelSectionPreview() {
    AIFitTheme(darkTheme = true) {
        VolumePanelSection(
            volumeByMuscleGroup = mapOf(
                MuscleGroup.CHEST to 4500.0,
                MuscleGroup.SHOULDERS to 2100.0,
                MuscleGroup.TRICEPS to 1200.0,
            ),
        )
    }
}

