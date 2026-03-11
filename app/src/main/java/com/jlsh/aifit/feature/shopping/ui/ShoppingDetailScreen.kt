package com.jlsh.aifit.feature.shopping.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.display.PlanStatusBadge
import com.jlsh.aifit.core.ui.components.feedback.ConfirmationDialog
import com.jlsh.aifit.core.ui.components.feedback.LoadingScreen
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.components.layout.SectionHeader
import com.jlsh.aifit.core.ui.components.list.CheckableListItem
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingCategory
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingCategoryGroup
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingItem
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingList
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingListPeriod
import com.jlsh.aifit.feature.shopping.ui.state.ShoppingDetailState
import com.jlsh.aifit.feature.shopping.ui.state.ShoppingUiEvent

@Composable
fun ShoppingDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ShoppingViewModel = hiltViewModel(),
) {
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ShoppingUiEvent.NavigateBack -> onNavigateBack()
                is ShoppingUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AiFitTopBar(
                title = "Lista de compras",
                onBack = onNavigateBack,
                background = MaterialTheme.colorScheme.background,
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        when {
            detailState.isLoading -> LoadingScreen()
            detailState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = detailState.error ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            detailState.list != null -> {
                ShoppingDetailContent(
                    paddingValues = paddingValues,
                    state = detailState,
                    onToggleCheck = { itemName, category ->
                        viewModel.onToggleCheck(detailState.list!!.id, itemName, category)
                    },
                )
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Eliminar lista",
            message = "¿Seguro que quieres eliminar esta lista de compras?",
            confirmText = "Eliminar",
            onConfirm = {
                showDeleteDialog = false
                viewModel.onDeleteCurrentList()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun ShoppingDetailContent(
    paddingValues: PaddingValues,
    state: ShoppingDetailState,
    onToggleCheck: (itemName: String, category: String) -> Unit,
) {
    val list = state.list ?: return

    LazyColumn(
        contentPadding = PaddingValues(
            start = AiFitSpacing.md,
            end = AiFitSpacing.md,
            top = AiFitSpacing.sm,
            bottom = 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
        modifier = Modifier.padding(paddingValues),
    ) {
        // Period badge
        item(key = "period") {
            PlanStatusBadge(status = list.period.name.replace("_", " "))
        }

        // Categories
        list.categories.forEach { group ->
            item(key = "header_${group.category.name}") {
                SectionHeader(title = group.category.name.replace("_", " "))
            }
            items(group.items, key = { "${group.category.name}:${it.name}" }) { item ->
                val checkKey = "${group.category.name}:${item.name}"
                val isChecked = state.checkStates[checkKey] ?: false
                CheckableListItem(
                    text = item.name,
                    checked = isChecked,
                    onCheckedChange = { onToggleCheck(item.name, group.category.name) },
                    subtitle = "${"%.0f".format(item.totalQuantity)} ${item.unit}${if (!item.notes.isNullOrBlank()) " · ${item.notes}" else ""}",
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "ShoppingDetail Dark",
)
@Composable
private fun ShoppingDetailPreview() {
    AIFitTheme(darkTheme = true) {
        ShoppingDetailContent(
            paddingValues = PaddingValues(),
            state = ShoppingDetailState(
                list = ShoppingList(
                    id = "1",
                    dietPlanId = "dp1",
                    period = ShoppingListPeriod.ONE_WEEK,
                    categories = listOf(
                        ShoppingCategoryGroup(
                            category = ShoppingCategory.PROTEINS,
                            items = listOf(
                                ShoppingItem("Pechuga de pollo", 1.5, "kg", null),
                                ShoppingItem("Huevos", 12.0, "unidades", "preferible de campo"),
                            ),
                        ),
                        ShoppingCategoryGroup(
                            category = ShoppingCategory.VEGETABLES,
                            items = listOf(
                                ShoppingItem("Espinacas", 300.0, "g", null),
                            ),
                        ),
                    ),
                    generatedAt = "2025-03-10T10:00:00Z",
                ),
                checkStates = mapOf("PROTEINS:Huevos" to true),
                isLoading = false,
            ),
            onToggleCheck = { _, _ -> },
        )
    }
}


