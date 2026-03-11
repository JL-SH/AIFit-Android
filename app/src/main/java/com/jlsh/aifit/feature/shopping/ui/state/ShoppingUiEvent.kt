package com.jlsh.aifit.feature.shopping.ui.state

sealed class ShoppingUiEvent {
    data class NavigateToDetail(val listId: String) : ShoppingUiEvent()
    data object NavigateBack : ShoppingUiEvent()
    data class ShowSnackbar(val message: String) : ShoppingUiEvent()
    data class ListGenerated(val listId: String) : ShoppingUiEvent()
}

