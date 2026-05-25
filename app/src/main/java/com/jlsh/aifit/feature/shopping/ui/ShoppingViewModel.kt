package com.jlsh.aifit.feature.shopping.ui

import android.util.Log
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
 * ViewModel del listado y detalle de listas de la compra.
 *
 * **UiState del listado** ([listState] — [ShoppingListUiState]):
 * - [ShoppingListUiState.Loading]: cargando listas.
 * - [ShoppingListUiState.Success]: listas disponibles.
 * - [ShoppingListUiState.Error]: mensaje de error.
 *
 * **UiState del detalle** ([detailState] — [ShoppingDetailState]):
 * - Lista cargada, estados de check, artículos locales y claves borradas.
 * - [ShoppingDetailState.isLoading]: carga del detalle.
 * - [ShoppingDetailState.isEditing]: modo edición con altas/bajas.
 * - [ShoppingDetailState.error]: error al cargar.
 *
 * **Eventos emitidos** ([events] — [ShoppingUiEvent]):
 * - [ShoppingUiEvent.NavigateToDetail]: abrir detalle de lista generada.
 * - [ShoppingUiEvent.NavigateBack]: volver tras eliminar la lista actual.
 * - [ShoppingUiEvent.ShowSnackbar]: mensaje al usuario.
 * - [ShoppingUiEvent.ListGenerated]: lista creada correctamente (navegación al detalle).
 *
 * @param savedStateHandle Lee `listId` de la ruta; si está presente carga detalle, si no, listado.
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

    /** Estado del listado de listas de la compra. */
    val listState: StateFlow<ShoppingListUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow(ShoppingDetailState())

    /** Estado del detalle de una lista (ítems, checks, edición). */
    val detailState: StateFlow<ShoppingDetailState> = _detailState.asStateFlow()

    private val _events = Channel<ShoppingUiEvent>(Channel.BUFFERED)

    /** Flujo de navegación y snackbars; consumir una vez por pantalla. */
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

    /** Recarga el listado de listas de la compra desde el repositorio. */
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
     * Elimina una lista del listado y muestra confirmación por snackbar.
     *
     * @param id Identificador de la lista a eliminar.
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
     * Genera una nueva lista de la compra y emite [ShoppingUiEvent.ListGenerated].
     *
     * @param dietPlanId Plan de dieta asociado, o `null` para generación genérica.
     * @param period Período temporal (valor API, p. ej. `ONE_WEEK`).
     */
    fun onGenerateList(dietPlanId: String?, period: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "onGenerateList start dietPlanId=$dietPlanId period=$period")
                val request = GenerateShoppingListRequestDto(dietPlanId = dietPlanId, period = period)
                when (val r = generateShoppingListUseCase(request)) {
                    is Result.Success -> {
                        Log.d(TAG, "onGenerateList success listId=${r.data.id}")
                        _events.send(ShoppingUiEvent.ListGenerated(r.data.id))
                    }
                    is Result.Error -> {
                        Log.w(TAG, "onGenerateList error: ${r.exception.toMessage()}")
                        _events.send(ShoppingUiEvent.ShowSnackbar(r.exception.toMessage()))
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                Log.e(TAG, "onGenerateList uncaught", e)
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

    // ── Detail ───────────────────────────────────────────────────────────────

    /**
     * Carga el detalle de una lista y sus flujos locales (checks, ítems, borrados).
     *
     * @param id Identificador de la lista de la compra.
     */
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
     * Marca o desmarca un artículo como comprado.
     *
     * @param listId Identificador de la lista.
     * @param itemName Nombre del artículo.
     * @param category Categoría del artículo.
     */
    fun onToggleCheck(listId: String, itemName: String, category: String) {
        val key = "$category:$itemName"
        val current = _detailState.value.checkStates[key] ?: false
        viewModelScope.launch {
            shoppingRepository.toggleCheck(listId, itemName, category, !current)
        }
    }

    /** Alterna el modo edición del detalle ([ShoppingDetailState.isEditing]). */
    fun onToggleEditMode() {
        _detailState.update { it.copy(isEditing = !it.isEditing) }
    }

    /**
     * Añade un artículo local a la lista actual (solo en modo edición).
     *
     * @param name Nombre del producto.
     * @param category Categoría (clave API).
     * @param quantity Cantidad numérica.
     * @param unit Unidad de medida (p. ej. "kg", "unidades").
     */
    fun onAddItem(name: String, category: String, quantity: Double, unit: String) {
        val id = listId ?: return
        viewModelScope.launch {
            shoppingRepository.addLocalItem(id, name, category, quantity, unit, null)
            _events.send(ShoppingUiEvent.ShowSnackbar("Artículo añadido"))
        }
    }

    /**
     * Elimina un artículo añadido localmente.
     *
     * @param localId Identificador local del artículo en Room.
     */
    fun onRemoveLocalItem(localId: Long) {
        viewModelScope.launch {
            shoppingRepository.deleteLocalItem(localId)
        }
    }

    /**
     * Oculta un artículo del servidor marcándolo como borrado localmente.
     *
     * @param itemName Nombre del artículo.
     * @param category Categoría del artículo.
     */
    fun onDeleteServerItem(itemName: String, category: String) {
        val id = listId ?: return
        viewModelScope.launch {
            shoppingRepository.markItemDeleted(id, itemName, category)
        }
    }

    /**
     * Restaura un artículo del servidor previamente oculto.
     *
     * @param itemName Nombre del artículo.
     * @param category Categoría del artículo.
     */
    fun onRestoreServerItem(itemName: String, category: String) {
        val id = listId ?: return
        viewModelScope.launch {
            shoppingRepository.unmarkItemDeleted(id, itemName, category)
        }
    }

    /** Elimina la lista del detalle actual y emite [ShoppingUiEvent.NavigateBack] si tiene éxito. */
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

