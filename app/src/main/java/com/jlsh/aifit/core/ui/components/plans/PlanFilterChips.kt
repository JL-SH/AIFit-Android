package com.jlsh.aifit.core.ui.components.plans

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.inputs.AiFitChipGroup
import com.jlsh.aifit.feature.training.domain.model.PlanStatus

val PLAN_FILTER_CHIPS = listOf("Todos", "Activo", "Completado", "Pausado")

fun planChipToStatus(chip: String): PlanStatus? = when (chip) {
    "Activo" -> PlanStatus.ACTIVE
    "Completado" -> PlanStatus.COMPLETED
    "Pausado" -> PlanStatus.PAUSED
    else -> null
}

fun planStatusToChip(status: PlanStatus?): String = when (status) {
    PlanStatus.ACTIVE -> "Activo"
    PlanStatus.COMPLETED -> "Completado"
    PlanStatus.PAUSED -> "Pausado"
    else -> "Todos"
}

@Composable
fun PlanFilterChipGroup(
    selectedFilter: PlanStatus?,
    onFilterChanged: (PlanStatus?) -> Unit,
) {
    val selectedChip = planStatusToChip(selectedFilter)
    AiFitChipGroup(
        options = PLAN_FILTER_CHIPS,
        selected = setOf(selectedChip),
        onSelectionChanged = { selection ->
            val chip = selection.firstOrNull() ?: "Todos"
            onFilterChanged(planChipToStatus(chip))
        },
        multiSelect = false,
        displayMapper = { chip ->
            when (chip) {
                "Activo" -> stringResource(R.string.plan_status_active)
                "Completado" -> stringResource(R.string.plan_status_completed)
                "Pausado" -> stringResource(R.string.plan_status_paused)
                else -> stringResource(R.string.plan_status_all)
            }
        },
    )
}
