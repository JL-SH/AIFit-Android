 package com.jlsh.aifit.feature.shopping.ui.state

import com.jlsh.aifit.core.ui.components.layout.UiStateHost
import com.jlsh.aifit.feature.shopping.data.local.ShoppingLocalItemEntity
import com.jlsh.aifit.feature.shopping.domain.model.ShoppingList

sealed class ShoppingListUiState {
    data object Loading : ShoppingListUiState(), UiStateHost.Loading
    data class Error(override val message: String) : ShoppingListUiState(), UiStateHost.Error
    data class Success(
        val lists: List<ShoppingList>,
    ) : ShoppingListUiState(), UiStateHost.Success
}

data class ShoppingDetailState(
    val list: ShoppingList? = null,
    val checkStates: Map<String, Boolean> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isEditing: Boolean = false,
    val localItems: List<ShoppingLocalItemEntity> = emptyList(),
    val deletedItemKeys: Set<String> = emptySet(),
)

