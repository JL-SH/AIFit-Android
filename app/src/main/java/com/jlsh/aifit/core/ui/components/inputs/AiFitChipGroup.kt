package com.jlsh.aifit.core.ui.components.inputs

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing

/**
 * A [FlowRow] of [FilterChip]s that supports single or multi-item selection.
 *
 * Selected chips use [MaterialTheme.colorScheme.primaryContainer] as background;
 * unselected chips use [MaterialTheme.colorScheme.surfaceVariant] with a subtle
 * outline. Chips wrap naturally into multiple rows when the available width
 * is exhausted.
 *
 * @param options Complete list of selectable option keys displayed as chips.
 * @param selected Set of option keys that are currently selected.
 * @param onSelectionChanged Callback invoked with the updated [Set] whenever
 *   a chip is toggled.
 * @param modifier Modifier applied to the outer [FlowRow].
 * @param multiSelect When `true`, multiple chips can be active simultaneously.
 *   When `false`, selecting a chip deselects all others (radio behavior).
 * @param displayMapper Composable function mapping a raw option key to its
 *   display label. Defaults to the key itself.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiFitChipGroup(
    options: List<String>,
    selected: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    multiSelect: Boolean = true,
    displayMapper: @Composable (String) -> String = { it },
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
    ) {
        options.forEach { option ->
            val isSelected = option in selected
            FilterChip(
                selected = isSelected,
                onClick = {
                    val newSelection = if (multiSelect) {
                        if (isSelected) selected - option else selected + option
                    } else {
                        if (isSelected) emptySet() else setOf(option)
                    }
                    onSelectionChanged(newSelection)
                },
                label = {
                    Text(
                        text = displayMapper(option),
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                shape = RoundedCornerShape(6.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                border = if (isSelected) {
                    null
                } else {
                    FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = false,
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        selectedBorderColor = Color.Transparent,
                    )
                },
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
private fun AiFitChipGroupPreview() {
    AIFitTheme {
        AiFitChipGroup(
            options = listOf("STRENGTH", "HYPERTROPHY", "ENDURANCE"),
            selected = setOf("STRENGTH"),
            onSelectionChanged = {},
            displayMapper = { it.lowercase().replaceFirstChar { c -> c.uppercase() } },
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark - MultiSelect"
)
@Composable
private fun AiFitChipGroupMultiSelectPreview() {
    AIFitTheme {
        AiFitChipGroup(
            options = listOf("Pecho", "Espalda", "Piernas", "Brazos"),
            selected = setOf("Pecho", "Piernas"),
            onSelectionChanged = {},
        )
    }
}
