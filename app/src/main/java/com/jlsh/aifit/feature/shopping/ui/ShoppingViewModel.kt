package com.jlsh.aifit.feature.shopping.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.feature.shopping.data.dto.GenerateShoppingListRequestDto
import com.jlsh.aifit.feature.shopping.domain.repository.ShoppingRepository
import com.jlsh.aifit.feature.shopping.domain.usecase.DeleteShoppingListUseCase
import com.jlsh.aifit.feature.shopping.domain.usecase.GenerateShoppingListUseCase
import com.jlsh.aifit.feature.shopping.domain.usecase.GetShoppingListUseCase
import com.jlsh.aifit.feature.shopping.domain.usecase.GetShoppingListsUseCase
import com.jlsh.aifit.feature.shopping.ui.state.ShoppingDetailState
import com.jlsh.aifit.feature.shopping.ui.state.ShoppingListUiState
import com.jlsh.aifit.feature.shopping.ui.state.ShoppingUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getShoppingListsUseCase: GetShoppingListsUseCase,
    private val getShoppingListUseCase: GetShoppingListUseCase,
    private val generateShoppingListUseCase: GenerateShoppingListUseCase,
    private val deleteShoppingListUseCase: DeleteShoppingListUseCase,
    private val shoppingRepository: ShoppingRepository,
) : ViewModel() {

    private val _listState = MutableStateFlow<ShoppingListUiState>(ShoppingListUiState.Loading)
    val listState: StateFlow<ShoppingListUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow(ShoppingDetailState())
    val detailState: StateFlow<ShoppingDetailState> = _detailState.asStateFlow()

    private val _events = Channel<ShoppingUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val listId: String? = savedStateHandle.get<String>("listId")

    init {
        if (listId != null) {
            loadDetail(listId)
        } else {
            loadLists()
        }
    }

    // ── Lists ────────────────────────────────────────────────────────────────

    fun loadLists() {
        viewModelScope.launch {
            getShoppingListsUseCase().collect { result ->
                when (result) {
                    is Result.Success -> _listState.value = ShoppingListUiState.Success(result.data)
                    is Result.Error -> _listState.value = ShoppingListUiState.Error(result.exception.toMessage())
                    is Result.Loading -> _listState.value = ShoppingListUiState.Loading
                }
            }
        }
    }

    fun onDeleteList(id: String) {
        viewModelScope.launch {
            when (val r = deleteShoppingListUseCase(id)) {
                is Result.Success -> {
                    loadLists()
                    _events.send(ShoppingUiEvent.ShowSnackbar("Lista eliminada"))
                }
                is Result.Error -> _events.send(ShoppingUiEvent.ShowSnackbar(r.exception.toMessage()))
                else -> Unit
            }
        }
    }

    fun onGenerateList(dietPlanId: String?, period: String) {
        viewModelScope.launch {
            val request = GenerateShoppingListRequestDto(dietPlanId = dietPlanId, period = period)
            when (val r = generateShoppingListUseCase(request)) {
                is Result.Success -> {
                    _events.send(ShoppingUiEvent.ListGenerated(r.data.id))
                }
                is Result.Error -> _events.send(ShoppingUiEvent.ShowSnackbar(r.exception.toMessage()))
                else -> Unit
            }
        }
    }

    // ── Detail ───────────────────────────────────────────────────────────────

    fun loadDetail(id: String) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true, error = null) }
            when (val r = getShoppingListUseCase(id)) {
                is Result.Success -> {
                    _detailState.update { it.copy(list = r.data, isLoading = false) }
                    // Collect check states
                    launch {
                        shoppingRepository.getCheckStates(id).collect { checks ->
                            _detailState.update { it.copy(checkStates = checks) }
                        }
                    }
                    // Collect local items
                    launch {
                        shoppingRepository.getLocalItems(id).collect { items ->
                            _detailState.update { it.copy(localItems = items) }
                        }
                    }
                    // Collect deleted item keys
                    launch {
                        shoppingRepository.getDeletedItemKeys(id).collect { keys ->
                            _detailState.update { it.copy(deletedItemKeys = keys) }
                        }
                    }
                }
                is Result.Error -> {
                    _detailState.update { it.copy(isLoading = false, error = r.exception.toMessage()) }
                }
                else -> Unit
            }
        }
    }

    fun onToggleCheck(listId: String, itemName: String, category: String) {
        val key = "$category:$itemName"
        val current = _detailState.value.checkStates[key] ?: false
        viewModelScope.launch {
            shoppingRepository.toggleCheck(listId, itemName, category, !current)
        }
    }

    fun onToggleEditMode() {
        _detailState.update { it.copy(isEditing = !it.isEditing) }
    }

    fun onAddItem(name: String, category: String, quantity: Double, unit: String) {
        val id = listId ?: return
        viewModelScope.launch {
            shoppingRepository.addLocalItem(id, name, category, quantity, unit, null)
            _events.send(ShoppingUiEvent.ShowSnackbar("Artículo añadido"))
        }
    }

    fun onRemoveLocalItem(localId: Long) {
        viewModelScope.launch {
            shoppingRepository.deleteLocalItem(localId)
        }
    }

    fun onDeleteServerItem(itemName: String, category: String) {
        val id = listId ?: return
        viewModelScope.launch {
            shoppingRepository.markItemDeleted(id, itemName, category)
        }
    }

    fun onRestoreServerItem(itemName: String, category: String) {
        val id = listId ?: return
        viewModelScope.launch {
            shoppingRepository.unmarkItemDeleted(id, itemName, category)
        }
    }

    fun onDeleteCurrentList() {
        val id = listId ?: return
        viewModelScope.launch {
            when (val r = deleteShoppingListUseCase(id)) {
                is Result.Success -> {
                    _events.send(ShoppingUiEvent.ShowSnackbar("Lista eliminada"))
                    _events.send(ShoppingUiEvent.NavigateBack)
                }
                is Result.Error -> _events.send(ShoppingUiEvent.ShowSnackbar(r.exception.toMessage()))
                else -> Unit
            }
        }
    }
}

