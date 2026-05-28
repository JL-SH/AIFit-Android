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

/**
 * ViewModel of the list and details of shopping lists.
 *
 * **ListingUiState** ([listState] — [ShoppingListUiState]):
 * - [ShoppingListUiState.Loading]: loading lists.
 * - [ShoppingListUiState.Success]: available lists.
 * - [ShoppingListUiState.Error]: error message.
 *
 * **Detail UiState** ([detailState] — [ShoppingDetailState]):
 * - Loaded list, check states, local items and deleted keys.
 * - [ShoppingDetailState.isLoading]: loading of the detail.
 * - [ShoppingDetailState.isEditing]: editing mode with additions/deletions.
 * - [ShoppingDetailState.error]: Error loading.
 *
 * **Emitted events** ([events] — [ShoppingUiEvent]):
 * - [ShoppingUiEvent.NavigateToDetail]: open generated list detail.
 * - [ShoppingUiEvent.NavigateBack]: go back after deleting the current list.
 * - [ShoppingUiEvent.ShowSnackbar]: message to the user.
 * - [ShoppingUiEvent.ListGenerated]: List created successfully (detail navigation).
 *
 * @param savedStateHandle Read `listId` from the path; if present load detail, if not, list.
 */
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

    /** Shopping list listing status.*/
    val listState: StateFlow<ShoppingListUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow(ShoppingDetailState())

    /** Detail status of a list (items, checks, edition).*/
    val detailState: StateFlow<ShoppingDetailState> = _detailState.asStateFlow()

    private val _events = Channel<ShoppingUiEvent>(Channel.BUFFERED)

    /** Navigation flow and snackbars; consume once per screen.*/
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

    /** Reload the list of shopping lists from the repository.*/
    fun loadLists() {
        viewModelScope.launch {
            getShoppingListsUseCase().collect { result ->
                when (result) {
                    is Result.Success -> _listState.value = ShoppingListUiState.Success(result.data)
                    is Result.Error -> _listState.value = ShoppingListUiState.Error(result.exception.toMessage())
                    is Result.Loading -> {
                        if (_listState.value !is ShoppingListUiState.Success) {
                            _listState.value = ShoppingListUiState.Loading
                        }
                    }
                }
            }
        }
    }

    /**
     * Removes a list from the list and shows confirmation by snackbar.
     *
     * @param id Identifier of the list to delete.
     */
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

    /**
     * Generate a new shopping list and emit [ShoppingUiEvent.ListGenerated].
     *
     * @param dietPlanId Associated diet plan, or `null` for generic generation.
     * @param period Time period (API value, e.g. `ONE_WEEK`).
     */
    fun onGenerateList(dietPlanId: String?, period: String) {
        viewModelScope.launch {
            try {
                safeLogDebug("onGenerateList start dietPlanId=$dietPlanId period=$period")
                val request = GenerateShoppingListRequestDto(dietPlanId = dietPlanId, period = period)
                when (val r = generateShoppingListUseCase(request)) {
                    is Result.Success -> {
                        safeLogDebug("onGenerateList success listId=${r.data.id}")
                        _events.send(ShoppingUiEvent.ListGenerated(r.data.id))
                    }
                    is Result.Error -> {
                        safeLogWarn("onGenerateList error: ${r.exception.toMessage()}")
                        _events.send(ShoppingUiEvent.ShowSnackbar(r.exception.toMessage()))
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                safeLogError("onGenerateList uncaught", e)
                _events.send(
                    ShoppingUiEvent.ShowSnackbar(
                        e.message ?: "Error al generar la lista de la compra",
                    ),
                )
            }
        }
    }

    private companion object {
        const val TAG = "AIFIT_SHOPPING"
    }

    private fun safeLogDebug(message: String) {
        runCatching { android.util.Log.d(TAG, message) }
    }

    private fun safeLogWarn(message: String) {
        runCatching { android.util.Log.w(TAG, message) }
    }

    private fun safeLogError(message: String, error: Throwable) {
        runCatching { android.util.Log.e(TAG, message, error) }
    }

    // ── Detail ───────────────────────────────────────────────────────────────

    /**
     * Load the details of a list and its local flows (checks, items, deletions).
     *
     * @param id Identifier of the shopping list.
     */
    /** Retry loading the active list detail after an error.*/
    fun retryDetailLoad() {
        listId?.let { loadDetail(it) }
    }

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

    /**
     * Mark or unmark an item as purchased.
     *
     * @param listId Identifier of the list.
     * @param itemName Name of the item.
     * @param category Item category.
     */
    fun onToggleCheck(listId: String, itemName: String, category: String) {
        val key = "$category:$itemName"
        val current = _detailState.value.checkStates[key] ?: false
        viewModelScope.launch {
            shoppingRepository.toggleCheck(listId, itemName, category, !current)
        }
    }

    /** Toggles the detail editing mode ([ShoppingDetailState.isEditing]).*/
    fun onToggleEditMode() {
        _detailState.update { it.copy(isEditing = !it.isEditing) }
    }

    /**
     * Adds a local item to the current list (in edit mode only).
     *
     * @param name Name of the product.
     * @param category Category (API key).
     * @param quantity Numeric quantity.
     * @param unit Unit of measure (e.g. "kg", "units").
     */
    fun onAddItem(name: String, category: String, quantity: Double, unit: String) {
        val id = listId ?: return
        viewModelScope.launch {
            shoppingRepository.addLocalItem(id, name, category, quantity, unit, null)
            _events.send(ShoppingUiEvent.ShowSnackbar("Artículo añadido"))
        }
    }

    /**
     * Delete a locally added item.
     *
     * @param localId Local identifier of the item in Room.
     */
    fun onRemoveLocalItem(localId: Long) {
        viewModelScope.launch {
            shoppingRepository.deleteLocalItem(localId)
        }
    }

    /**
     * Hide an article from the server by marking it as locally deleted.
     *
     * @param itemName Name of the item.
     * @param category Item category.
     */
    fun onDeleteServerItem(itemName: String, category: String) {
        val id = listId ?: return
        viewModelScope.launch {
            shoppingRepository.markItemDeleted(id, itemName, category)
        }
    }

    /**
     * Restores a previously hidden server item.
     *
     * @param itemName Name of the item.
     * @param category Item category.
     */
    fun onRestoreServerItem(itemName: String, category: String) {
        val id = listId ?: return
        viewModelScope.launch {
            shoppingRepository.unmarkItemDeleted(id, itemName, category)
        }
    }

    /** Removes the list from the current detail and issues [ShoppingUiEvent.NavigateBack] if successful.*/
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

