package com.jlsh.aifit.feature.shopping.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.display.PlanStatusBadge
import com.jlsh.aifit.core.ui.components.feedback.ConfirmationDialog
import com.jlsh.aifit.core.ui.components.feedback.LoadingScreen
import com.jlsh.aifit.core.ui.components.inputs.AiFitDropdown
import com.jlsh.aifit.core.ui.components.inputs.AiFitNumberField
import com.jlsh.aifit.core.ui.components.inputs.AiFitTextField
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.components.layout.SectionHeader
import com.jlsh.aifit.core.ui.components.list.CheckableListItem
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.shopping.data.local.ShoppingLocalItemEntity
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingCategory
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingCategoryGroup
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingItem
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingList
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingListPeriod
import com.jlsh.aifit.feature.shopping.ui.state.ShoppingDetailState
import com.jlsh.aifit.feature.shopping.ui.state.ShoppingUiEvent
import com.jlsh.aifit.feature.user.ui.toStringRes

/**
 * Pantalla de detalle de una lista de la compra con checklist por categorías.
 *
 * Muestra badge de período, ítems marcables del servidor y locales, modo edición con altas/bajas
 * y sección de ítems eliminados recuperables. Incluye diálogo de confirmación para borrar la lista.
 *
 * @param onNavigateBack Vuelve al listado o pantalla anterior.
 * @param viewModel ViewModel de compras inyectado por Hilt (`listId` en la ruta).
 */
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
                title = stringResource(R.string.shopping_title),
                onBack = onNavigateBack,
                background = MaterialTheme.colorScheme.background,
                actions = {
                    // Edit mode toggle
                    if (detailState.list != null) {
                        IconButton(onClick = { viewModel.onToggleEditMode() }) {
                            Icon(
                                imageVector = if (detailState.isEditing) Icons.Rounded.Close else Icons.Rounded.Edit,
                                contentDescription = if (detailState.isEditing) stringResource(R.string.shopping_close_edit_cd) else stringResource(R.string.shopping_edit_cd),
                                tint = if (detailState.isEditing) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        }
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(R.string.shopping_delete_cd),
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
                    onDeleteServerItem = { itemName, category ->
                        viewModel.onDeleteServerItem(itemName, category)
                    },
                    onRestoreServerItem = { itemName, category ->
                        viewModel.onRestoreServerItem(itemName, category)
                    },
                    onRemoveLocalItem = { localId ->
                        viewModel.onRemoveLocalItem(localId)
                    },
                    onAddItem = { name, category, quantity, unit ->
                        viewModel.onAddItem(name, category, quantity, unit)
                    },
                )
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.shopping_delete_title),
            message = stringResource(R.string.shopping_delete_message),
            confirmText = stringResource(R.string.shopping_delete_confirm),
            onConfirm = {
                showDeleteDialog = false
                viewModel.onDeleteCurrentList()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

// ── Merge helpers ─────────────────────────────────────────────────────────────

private data class MergedCategory(
    val categoryName: String,
    val serverItems: List<ShoppingItem>,
    val localItems: List<ShoppingLocalItemEntity>,
)

private fun buildMergedCategories(
    serverCategories: List<ShoppingCategoryGroup>,
    localItems: List<ShoppingLocalItemEntity>,
    deletedKeys: Set<String>,
): List<MergedCategory> {
    val localByCategory = localItems.groupBy { it.category }
    val seenCategories = mutableSetOf<String>()
    val result = mutableListOf<MergedCategory>()

    for (group in serverCategories) {
        val catName = group.category.name
        seenCategories.add(catName)
        val filteredItems = group.items.filter { item -> "$catName:${item.name}" !in deletedKeys }
        val locals = localByCategory[catName] ?: emptyList()
        if (filteredItems.isNotEmpty() || locals.isNotEmpty()) {
            result.add(MergedCategory(catName, filteredItems, locals))
        }
    }

    for ((catName, locals) in localByCategory) {
        if (catName !in seenCategories && locals.isNotEmpty()) {
            result.add(MergedCategory(catName, emptyList(), locals))
        }
    }

    return result
}

@Composable
private fun ShoppingDetailContent(
    paddingValues: PaddingValues,
    state: ShoppingDetailState,
    onToggleCheck: (itemName: String, category: String) -> Unit,
    onDeleteServerItem: (itemName: String, category: String) -> Unit,
    onRestoreServerItem: (itemName: String, category: String) -> Unit,
    onRemoveLocalItem: (localId: Long) -> Unit,
    onAddItem: (name: String, category: String, quantity: Double, unit: String) -> Unit,
) {
    val list = state.list ?: return
    val isEditing = state.isEditing

    // Build merged categories: server items (minus deleted) + local items
    val mergedCategories: List<MergedCategory> = remember(list.categories, state.localItems, state.deletedItemKeys) {
        buildMergedCategories(list.categories, state.localItems, state.deletedItemKeys)
    }

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
            PlanStatusBadge(status = stringResource(list.period.toStringRes()))
        }

        // Edit mode banner
        item(key = "edit_banner") {
            AnimatedVisibility(
                visible = isEditing,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
            ) {
                Text(
                    text = stringResource(R.string.shopping_edit_banner),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = AiFitSpacing.xs),
                )
            }
        }

        // Deleted items (shown in edit mode for recovery)
        if (isEditing && state.deletedItemKeys.isNotEmpty()) {
            item(key = "deleted_header") {
                SectionHeader(title = stringResource(R.string.shopping_deleted_header))
            }
            val deletedPairs = state.deletedItemKeys.map { key ->
                val parts = key.split(":", limit = 2)
                parts[0] to parts.getOrElse(1) { "" }
            }
            items(deletedPairs, key = { "deleted_${it.first}:${it.second}" }) { (cat, name) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AiFitSpacing.xs, horizontal = AiFitSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .alpha(0.5f),
                    )
                    IconButton(onClick = { onRestoreServerItem(name, cat) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Undo,
                            contentDescription = stringResource(R.string.shopping_restore_cd),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        // Categories (merged)
        mergedCategories.forEach { merged: MergedCategory ->
            item(key = "header_${merged.categoryName}") {
                SectionHeader(
                    title = categoryDisplayName(merged.categoryName),
                )
            }

            // Server items (not deleted)
            items(merged.serverItems, key = { "${merged.categoryName}:${it.name}" }) { item ->
                val checkKey = "${merged.categoryName}:${item.name}"
                val isChecked = state.checkStates[checkKey] ?: false

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        CheckableListItem(
                            text = item.name,
                            checked = isChecked,
                            onCheckedChange = { onToggleCheck(item.name, merged.categoryName) },
                            subtitle = "${"%.0f".format(item.totalQuantity)} ${item.unit}${if (!item.notes.isNullOrBlank()) " · ${item.notes}" else ""}",
                        )
                    }
                    if (isEditing) {
                        IconButton(onClick = { onDeleteServerItem(item.name, merged.categoryName) }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.shopping_remove_cd),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            // Local items
            items(merged.localItems, key = { "local_${it.localId}" }) { localItem ->
                val checkKey = "${localItem.category}:${localItem.itemName}"
                val isChecked = state.checkStates[checkKey] ?: false

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        CheckableListItem(
                            text = "✚ ${localItem.itemName}",
                            checked = isChecked,
                            onCheckedChange = { onToggleCheck(localItem.itemName, localItem.category) },
                            subtitle = "${"%.0f".format(localItem.totalQuantity)} ${localItem.unit}",
                        )
                    }
                    if (isEditing) {
                        IconButton(onClick = { onRemoveLocalItem(localItem.localId) }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.shopping_remove_cd),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }

        // Add item form (only in edit mode)
        item(key = "add_item_form") {
            AnimatedVisibility(
                visible = isEditing,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
            ) {
                AddItemForm(
                    categories = list.categories.map { it.category.name },
                    onAddItem = onAddItem,
                )
            }
        }
    }
}

// ── Add Item Form ─────────────────────────────────────────────────────────────

@Composable
private fun AddItemForm(
    categories: List<String>,
    onAddItem: (name: String, category: String, quantity: Double, unit: String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "OTHER") }

    // Resolve category display names inside composable scope
    val categoryDisplayMap = mapOf(
        "PROTEINS" to stringResource(R.string.shopping_category_proteins),
        "VEGETABLES" to stringResource(R.string.shopping_category_vegetables),
        "FRUITS" to stringResource(R.string.shopping_category_fruits),
        "GRAINS_AND_CARBS" to stringResource(R.string.shopping_category_grains),
        "DAIRY" to stringResource(R.string.shopping_category_dairy),
        "FATS_AND_OILS" to stringResource(R.string.shopping_category_fats),
        "CONDIMENTS_AND_SPICES" to stringResource(R.string.shopping_category_condiments),
        "OTHER" to stringResource(R.string.shopping_category_other),
        "UNKNOWN" to stringResource(R.string.shopping_category_unknown),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AiFitSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
    ) {
        SectionHeader(title = stringResource(R.string.shopping_add_item_header))

        AiFitTextField(
            value = name,
            onValueChange = { name = it },
            label = stringResource(R.string.shopping_item_name_label),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
            modifier = Modifier.fillMaxWidth(),
        ) {
            AiFitNumberField(
                value = quantity,
                onValueChange = { quantity = it },
                label = stringResource(R.string.shopping_quantity_label),
                modifier = Modifier.weight(1f),
            )
            AiFitTextField(
                value = unit,
                onValueChange = { unit = it },
                label = stringResource(R.string.shopping_unit_label),
                modifier = Modifier.weight(1f),
            )
        }

        AiFitDropdown(
            selectedValue = selectedCategory,
            options = categories.ifEmpty { listOf("OTHER") },
            onOptionSelected = { selectedCategory = it },
            label = stringResource(R.string.shopping_category_label),
            displayMapper = { categoryDisplayMap[it] ?: it.replace("_", " ") },
        )

        Spacer(modifier = Modifier.height(AiFitSpacing.xs))

        val canAdd = name.isNotBlank() && quantity.isNotBlank() && unit.isNotBlank()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            IconButton(
                onClick = {
                    if (canAdd) {
                        onAddItem(name.trim(), selectedCategory, quantity.toDoubleOrNull() ?: 1.0, unit.trim())
                        name = ""
                        quantity = ""
                        unit = ""
                    }
                },
                enabled = canAdd,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.shopping_add_cd),
                    tint = if (canAdd) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

// ── Category display helper ───────────────────────────────────────────────────

@Composable
private fun categoryDisplayName(key: String): String = when (key) {
    "PROTEINS" -> stringResource(R.string.shopping_category_proteins)
    "VEGETABLES" -> stringResource(R.string.shopping_category_vegetables)
    "FRUITS" -> stringResource(R.string.shopping_category_fruits)
    "GRAINS_AND_CARBS" -> stringResource(R.string.shopping_category_grains)
    "DAIRY" -> stringResource(R.string.shopping_category_dairy)
    "FATS_AND_OILS" -> stringResource(R.string.shopping_category_fats)
    "CONDIMENTS_AND_SPICES" -> stringResource(R.string.shopping_category_condiments)
    "OTHER" -> stringResource(R.string.shopping_category_other)
    "UNKNOWN" -> stringResource(R.string.shopping_category_unknown)
    else -> key.replace("_", " ")
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
            onDeleteServerItem = { _, _ -> },
            onRestoreServerItem = { _, _ -> },
            onRemoveLocalItem = {},
            onAddItem = { _, _, _, _ -> },
        )
    }
}
