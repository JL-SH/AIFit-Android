package com.jlsh.aifit.core.ui.components.inputs

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jlsh.aifit.core.ui.theme.AIFitTheme

/**
 * Themed [ExposedDropdownMenuBox] styled to match the AIFit design system.
 *
 * Renders a read-only [OutlinedTextField] with an animated chevron icon that
 * rotates 180° when the menu is open. The currently selected item is highlighted
 * with [MaterialTheme.colorScheme.primaryContainer] in the dropdown list.
 *
 * @param selectedValue The currently selected option key shown in the field.
 * @param options Complete list of selectable option keys.
 * @param onOptionSelected Callback invoked with the key of the option tapped by
 *   the user; the menu closes automatically.
 * @param label Floating label rendered above the field.
 * @param modifier Modifier applied to the [ExposedDropdownMenuBox].
 * @param displayMapper Function that converts a raw option key to a human-readable
 *   display label. Defaults to the key itself.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiFitDropdown(
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    displayMapper: (String) -> String = { it },
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation",
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = if (selectedValue.isNotEmpty()) displayMapper(selectedValue) else "",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            label = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primaryContainer,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            shape = RoundedCornerShape(8.dp),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = displayMapper(option),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (option == selectedValue) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark"
)
@Composable
private fun AiFitDropdownPreview() {
    AIFitTheme {
        AiFitDropdown(
            selectedValue = "BEGINNER",
            options = listOf("BEGINNER", "INTERMEDIATE", "ADVANCED"),
            onOptionSelected = {},
            label = "Nivel",
            displayMapper = { it.lowercase().replaceFirstChar { c -> c.uppercase() } },
        )
    }
}
