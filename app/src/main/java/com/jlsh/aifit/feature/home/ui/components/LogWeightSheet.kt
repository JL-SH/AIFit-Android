package com.jlsh.aifit.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.inputs.AiFitNumberField
import com.jlsh.aifit.core.ui.theme.AiFitSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogWeightSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onConfirm: (weight: Double) -> Unit,
) {
    var weightValue by rememberSaveable { mutableStateOf("") }

    val parsedWeight = weightValue.toDoubleOrNull()
    val isValid = parsedWeight != null && parsedWeight > 0.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge,
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(AiFitSpacing.sm))
                Spacer(
                    modifier = Modifier
                        .size(width = 32.dp, height = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(2.dp),
                        ),
                )
                Spacer(modifier = Modifier.height(AiFitSpacing.sm))
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AiFitSpacing.md)
                .padding(bottom = AiFitSpacing.xl),
        ) {
            Text(
                text = "Registrar peso",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(AiFitSpacing.lg))

            AiFitNumberField(
                value = weightValue,
                onValueChange = { weightValue = it },
                label = "Peso actual",
                suffix = "kg",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(AiFitSpacing.lg))

            PrimaryButton(
                text = "GUARDAR",
                onClick = {
                    parsedWeight?.let { onConfirm(it) }
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(AiFitSpacing.md))
        }
    }
}


