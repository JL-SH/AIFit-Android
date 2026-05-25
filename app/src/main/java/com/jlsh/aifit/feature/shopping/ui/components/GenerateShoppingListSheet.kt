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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.buttons.AiGenerateButton
import com.jlsh.aifit.core.ui.components.inputs.AiFitChipGroup
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.training.domain.model.PlanStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateShoppingListSheet(
    sheetState: SheetState,
    dietPlans: List<DietPlan>,
    resetLoadingSignal: Int,
    onDismiss: () -> Unit,
    onGenerate: (dietPlanId: String?, period: String) -> Unit,
) {
    val periodOptions = listOf("ONE_WEEK", "TWO_WEEKS", "ONE_MONTH")

    var selectedPeriod by remember { mutableStateOf(setOf("ONE_WEEK")) }
    val activePlans = dietPlans.filter { it.status == PlanStatus.ACTIVE }
    var selectedPlanId by remember(activePlans.map { it.id }) {
        mutableStateOf(activePlans.firstOrNull()?.id.orEmpty())
    }
    var isGenerating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(activePlans.map { it.id }) {
        if (selectedPlanId.isBlank() || activePlans.none { it.id == selectedPlanId }) {
            selectedPlanId = activePlans.firstOrNull()?.id.orEmpty()
        }
    }

    LaunchedEffect(resetLoadingSignal) {
        if (resetLoadingSignal > 0) {
            isGenerating = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (!isGenerating) onDismiss()
        },
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
                text = stringResource(R.string.shopping_generate_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = stringResource(R.string.shopping_generate_period_label),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AiFitChipGroup(
                options = periodOptions,
                selected = selectedPeriod,
                onSelectionChanged = { selectedPeriod = it },
                multiSelect = false,
                displayMapper = { key ->
                    when (key) {
                        "ONE_WEEK" -> stringResource(R.string.shopping_generate_period_weekly)
                        "TWO_WEEKS" -> stringResource(R.string.shopping_generate_period_biweekly)
                        "ONE_MONTH" -> stringResource(R.string.shopping_generate_period_monthly)
                        else -> key
                    }
                },
            )

            if (activePlans.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.shopping_generate_plan_label),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (activePlans.size == 1) {
                    Text(
                        text = activePlans.first().name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                } else {
                    AiFitChipGroup(
                        options = activePlans.map { it.id },
                        selected = setOf(selectedPlanId).filter { it.isNotEmpty() }.toSet(),
                        onSelectionChanged = { selection ->
                            selectedPlanId = selection.firstOrNull().orEmpty()
                        },
                        multiSelect = false,
                        displayMapper = { id -> activePlans.find { it.id == id }?.name ?: id },
                    )
                }
            }

            Spacer(modifier = Modifier.height(AiFitSpacing.sm))

            AiGenerateButton(
                text = stringResource(R.string.shopping_generate_btn),
                onClick = {
                    if (isGenerating) return@AiGenerateButton
                    val period = selectedPeriod.firstOrNull() ?: "ONE_WEEK"
                    val planId = selectedPlanId.ifBlank { null }
                    isGenerating = true
                    scope.launch {
                        onGenerate(planId, period)
                    }
                },
                isLoading = isGenerating,
                loadingText = stringResource(R.string.shopping_generate_loading),
                enabled = !isGenerating,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
