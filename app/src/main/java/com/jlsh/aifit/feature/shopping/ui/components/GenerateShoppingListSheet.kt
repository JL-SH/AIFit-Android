package com.jlsh.aifit.feature.shopping.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jlsh.aifit.core.ui.components.buttons.AiGenerateButton
import com.jlsh.aifit.core.ui.components.inputs.AiFitChipGroup
import com.jlsh.aifit.core.ui.components.inputs.AiFitDropdown
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.training.domain.model.PlanStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateShoppingListSheet(
    sheetState: SheetState,
    dietPlans: List<DietPlan>,
    isGenerating: Boolean,
    onDismiss: () -> Unit,
    onGenerate: (dietPlanId: String?, period: String) -> Unit,
) {
    val periodOptions = listOf("ONE_WEEK", "TWO_WEEKS", "ONE_MONTH")
    val periodLabels = mapOf(
        "ONE_WEEK" to "Semanal",
        "TWO_WEEKS" to "Quincenal",
        "ONE_MONTH" to "Mensual",
    )

    var selectedPeriod by remember { mutableStateOf(setOf("ONE_WEEK")) }
    val activePlans = dietPlans.filter { it.status == PlanStatus.ACTIVE }
    var selectedPlanId by remember {
        mutableStateOf(activePlans.firstOrNull()?.id ?: "")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AiFitSpacing.md)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
        ) {
            Text(
                text = "Generar lista de compras",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            // Period selector
            Text(
                text = "Período",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AiFitChipGroup(
                options = periodOptions,
                selected = selectedPeriod,
                onSelectionChanged = { selectedPeriod = it },
                multiSelect = false,
                displayMapper = { periodLabels[it] ?: it },
            )

            // Diet plan selector
            if (activePlans.isNotEmpty()) {
                Text(
                    text = "Plan de dieta",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AiFitDropdown(
                    selectedValue = selectedPlanId,
                    options = activePlans.map { it.id },
                    onOptionSelected = { selectedPlanId = it },
                    label = "Seleccionar plan",
                    displayMapper = { id -> activePlans.find { it.id == id }?.name ?: id },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(AiFitSpacing.sm))

            AiGenerateButton(
                text = "Generar lista",
                onClick = {
                    val period = selectedPeriod.firstOrNull() ?: "ONE_WEEK"
                    val planId = selectedPlanId.ifBlank { null }
                    onGenerate(planId, period)
                },
                isLoading = isGenerating,
                loadingText = "Generando…",
                enabled = !isGenerating,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}



