package com.jlsh.aifit.feature.shopping.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.shopping.domain.repository.ShoppingRepository
import com.jlsh.aifit.feature.shopping.domain.usecase.DeleteShoppingListUseCase
import com.jlsh.aifit.feature.shopping.domain.usecase.GenerateShoppingListUseCase
import com.jlsh.aifit.feature.shopping.domain.usecase.GetShoppingListUseCase
import com.jlsh.aifit.feature.shopping.domain.usecase.GetShoppingListsUseCase
import com.jlsh.aifit.feature.shopping.ui.state.ShoppingListUiState
import com.jlsh.aifit.feature.shopping.ui.state.ShoppingUiEvent
import com.jlsh.aifit.testutil.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getShoppingListsUseCase: GetShoppingListsUseCase = mockk()
    private val getShoppingListUseCase: GetShoppingListUseCase = mockk()
    private val generateShoppingListUseCase: GenerateShoppingListUseCase = mockk()
    private val deleteShoppingListUseCase: DeleteShoppingListUseCase = mockk()
    private val shoppingRepository: ShoppingRepository = mockk(relaxUnitFun = true)

    private fun buildViewModel(listId: String? = null): ShoppingViewModel {
        val savedState = SavedStateHandle().apply {
            if (listId != null) set("listId", listId)
        }
        return ShoppingViewModel(
            savedStateHandle = savedState,
            getShoppingListsUseCase = getShoppingListsUseCase,
            getShoppingListUseCase = getShoppingListUseCase,
            generateShoppingListUseCase = generateShoppingListUseCase,
            deleteShoppingListUseCase = deleteShoppingListUseCase,
            shoppingRepository = shoppingRepository,
        )
    }

    // ── List mode (sin listId) ──────────────────────────────────────────────

    @Test
    fun `sin listId, init carga listas`() = runTest {
        val lists = listOf(fakeShoppingList())
        every { getShoppingListsUseCase() } returns flowOf(Result.Success(lists))

        val vm = buildViewModel()
        advanceUntilIdle()

        val state = vm.listState.value
        assertTrue(state is ShoppingListUiState.Success)
        assertEquals(1, (state as ShoppingListUiState.Success).lists.size)
    }

    @Test
    fun `loadLists con error produce ShoppingListUiState Error`() = runTest {
        every { getShoppingListsUseCase() } returns flowOf(Result.Error(AppException.NetworkException))

        val vm = buildViewModel()
        advanceUntilIdle()

        assertTrue(vm.listState.value is ShoppingListUiState.Error)
    }

    @Test
    fun `loadLists con Loading produce ShoppingListUiState Loading`() = runTest {
        every { getShoppingListsUseCase() } returns flowOf(Result.Loading)

        val vm = buildViewModel()
        advanceUntilIdle()

        assertTrue(vm.listState.value is ShoppingListUiState.Loading)
    }

    // ── onDeleteList ────────────────────────────────────────────────────────

    @Test
    fun `onDeleteList exitoso recarga listas y envia snackbar`() = runTest {
        every { getShoppingListsUseCase() } returns flowOf(Result.Success(emptyList()))
        coEvery { deleteShoppingListUseCase("slist-1") } returns Result.Success(Unit)

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onDeleteList("slist-1")
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is ShoppingUiEvent.ShowSnackbar)
            assertEquals("Lista eliminada", (event as ShoppingUiEvent.ShowSnackbar).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDeleteList fallido envia snackbar de error`() = runTest {
        every { getShoppingListsUseCase() } returns flowOf(Result.Success(emptyList()))
        coEvery { deleteShoppingListUseCase("slist-1") } returns Result.Error(AppException.ServerException)

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onDeleteList("slist-1")
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is ShoppingUiEvent.ShowSnackbar)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── onGenerateList ──────────────────────────────────────────────────────

    @Test
    fun `onGenerateList exitoso envia evento ListGenerated`() = runTest {
        every { getShoppingListsUseCase() } returns flowOf(Result.Success(emptyList()))
        coEvery { generateShoppingListUseCase(any()) } returns Result.Success(fakeShoppingList(id = "new-list"))

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onGenerateList("diet-plan-1", "ONE_WEEK")
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is ShoppingUiEvent.ListGenerated)
            assertEquals("new-list", (event as ShoppingUiEvent.ListGenerated).listId)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onGenerateList fallido envia snackbar de error`() = runTest {
        every { getShoppingListsUseCase() } returns flowOf(Result.Success(emptyList()))
        coEvery { generateShoppingListUseCase(any()) } returns Result.Error(AppException.ServerException)

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onGenerateList(null, "ONE_WEEK")
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is ShoppingUiEvent.ShowSnackbar)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Detail mode (con listId) ────────────────────────────────────────────

    @Test
    fun `con listId, init carga detalle y checkStates`() = runTest {
        val list = fakeShoppingList(id = "slist-1")
        coEvery { getShoppingListUseCase("slist-1") } returns Result.Success(list)
        every { shoppingRepository.getCheckStates("slist-1") } returns flowOf(
            mapOf("PROTEINS:Chicken" to true)
        )
        every { shoppingRepository.getLocalItems("slist-1") } returns flowOf(emptyList())
        every { shoppingRepository.getDeletedItemKeys("slist-1") } returns flowOf(emptySet())

        val vm = buildViewModel(listId = "slist-1")
        advanceUntilIdle()

        val state = vm.detailState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNotNull(state.list)
        assertEquals("slist-1", state.list?.id)
        assertEquals(true, state.checkStates["PROTEINS:Chicken"])
    }

    @Test
    fun `loadDetail con error actualiza detailState con error`() = runTest {
        coEvery { getShoppingListUseCase("slist-1") } returns Result.Error(AppException.ServerException)

        val vm = buildViewModel(listId = "slist-1")
        advanceUntilIdle()

        val state = vm.detailState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }

    // ── onToggleCheck ───────────────────────────────────────────────────────

    @Test
    fun `onToggleCheck llama toggleCheck en repository con valor invertido`() = runTest {
        val list = fakeShoppingList(id = "slist-1")
        coEvery { getShoppingListUseCase("slist-1") } returns Result.Success(list)
        every { shoppingRepository.getCheckStates("slist-1") } returns flowOf(
            mapOf("PROTEINS:Chicken" to false)
        )
        every { shoppingRepository.getLocalItems("slist-1") } returns flowOf(emptyList())
        every { shoppingRepository.getDeletedItemKeys("slist-1") } returns flowOf(emptySet())

        val vm = buildViewModel(listId = "slist-1")
        advanceUntilIdle()

        vm.onToggleCheck("slist-1", "Chicken", "PROTEINS")
        advanceUntilIdle()

        coVerify { shoppingRepository.toggleCheck("slist-1", "Chicken", "PROTEINS", true) }
    }

    @Test
    fun `onToggleCheck con item ya marcado lo desmarca`() = runTest {
        val list = fakeShoppingList(id = "slist-1")
        coEvery { getShoppingListUseCase("slist-1") } returns Result.Success(list)
        every { shoppingRepository.getCheckStates("slist-1") } returns flowOf(
            mapOf("PROTEINS:Chicken" to true)
        )
        every { shoppingRepository.getLocalItems("slist-1") } returns flowOf(emptyList())
        every { shoppingRepository.getDeletedItemKeys("slist-1") } returns flowOf(emptySet())

        val vm = buildViewModel(listId = "slist-1")
        advanceUntilIdle()

        vm.onToggleCheck("slist-1", "Chicken", "PROTEINS")
        advanceUntilIdle()

        coVerify { shoppingRepository.toggleCheck("slist-1", "Chicken", "PROTEINS", false) }
    }

    // ── onDeleteCurrentList ─────────────────────────────────────────────────

    @Test
    fun `onDeleteCurrentList exitoso envia snackbar y NavigateBack`() = runTest {
        val list = fakeShoppingList(id = "slist-1")
        coEvery { getShoppingListUseCase("slist-1") } returns Result.Success(list)
        every { shoppingRepository.getCheckStates("slist-1") } returns flowOf(emptyMap())
        every { shoppingRepository.getLocalItems("slist-1") } returns flowOf(emptyList())
        every { shoppingRepository.getDeletedItemKeys("slist-1") } returns flowOf(emptySet())
        coEvery { deleteShoppingListUseCase("slist-1") } returns Result.Success(Unit)

        val vm = buildViewModel(listId = "slist-1")
        advanceUntilIdle()

        vm.events.test {
            vm.onDeleteCurrentList()
            advanceUntilIdle()

            val snackbar = awaitItem()
            assertTrue(snackbar is ShoppingUiEvent.ShowSnackbar)
            assertEquals("Lista eliminada", (snackbar as ShoppingUiEvent.ShowSnackbar).message)

            val nav = awaitItem()
            assertTrue(nav is ShoppingUiEvent.NavigateBack)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDeleteCurrentList fallido envia snackbar de error`() = runTest {
        val list = fakeShoppingList(id = "slist-1")
        coEvery { getShoppingListUseCase("slist-1") } returns Result.Success(list)
        every { shoppingRepository.getCheckStates("slist-1") } returns flowOf(emptyMap())
        every { shoppingRepository.getLocalItems("slist-1") } returns flowOf(emptyList())
        every { shoppingRepository.getDeletedItemKeys("slist-1") } returns flowOf(emptySet())
        coEvery { deleteShoppingListUseCase("slist-1") } returns Result.Error(AppException.ServerException)

        val vm = buildViewModel(listId = "slist-1")
        advanceUntilIdle()

        vm.events.test {
            vm.onDeleteCurrentList()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is ShoppingUiEvent.ShowSnackbar)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDeleteCurrentList sin listId no hace nada`() = runTest {
        every { getShoppingListsUseCase() } returns flowOf(Result.Success(emptyList()))

        val vm = buildViewModel(listId = null)
        advanceUntilIdle()

        // Should be a no-op
        vm.onDeleteCurrentList()
        advanceUntilIdle()
    }

    // ── Edit mode ──────────────────────────────────────────────────────────────

    @Test
    fun `onToggleEditMode alterna isEditing`() = runTest {
        val list = fakeShoppingList(id = "slist-1")
        coEvery { getShoppingListUseCase("slist-1") } returns Result.Success(list)
        every { shoppingRepository.getCheckStates("slist-1") } returns flowOf(emptyMap())
        every { shoppingRepository.getLocalItems("slist-1") } returns flowOf(emptyList())
        every { shoppingRepository.getDeletedItemKeys("slist-1") } returns flowOf(emptySet())

        val vm = buildViewModel(listId = "slist-1")
        advanceUntilIdle()

        assertFalse(vm.detailState.value.isEditing)

        vm.onToggleEditMode()
        assertTrue(vm.detailState.value.isEditing)

        vm.onToggleEditMode()
        assertFalse(vm.detailState.value.isEditing)
    }

    @Test
    fun `onAddItem llama addLocalItem en repository y envia snackbar`() = runTest {
        val list = fakeShoppingList(id = "slist-1")
        coEvery { getShoppingListUseCase("slist-1") } returns Result.Success(list)
        every { shoppingRepository.getCheckStates("slist-1") } returns flowOf(emptyMap())
        every { shoppingRepository.getLocalItems("slist-1") } returns flowOf(emptyList())
        every { shoppingRepository.getDeletedItemKeys("slist-1") } returns flowOf(emptySet())

        val vm = buildViewModel(listId = "slist-1")
        advanceUntilIdle()

        vm.events.test {
            vm.onAddItem("Tomate", "VEGETABLES", 2.0, "kg")
            advanceUntilIdle()

            coVerify { shoppingRepository.addLocalItem("slist-1", "Tomate", "VEGETABLES", 2.0, "kg", null) }

            val event = awaitItem()
            assertTrue(event is ShoppingUiEvent.ShowSnackbar)
            assertEquals("Artículo añadido", (event as ShoppingUiEvent.ShowSnackbar).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onRemoveLocalItem llama deleteLocalItem en repository`() = runTest {
        val list = fakeShoppingList(id = "slist-1")
        coEvery { getShoppingListUseCase("slist-1") } returns Result.Success(list)
        every { shoppingRepository.getCheckStates("slist-1") } returns flowOf(emptyMap())
        every { shoppingRepository.getLocalItems("slist-1") } returns flowOf(emptyList())
        every { shoppingRepository.getDeletedItemKeys("slist-1") } returns flowOf(emptySet())

        val vm = buildViewModel(listId = "slist-1")
        advanceUntilIdle()

        vm.onRemoveLocalItem(42L)
        advanceUntilIdle()

        coVerify { shoppingRepository.deleteLocalItem(42L) }
    }

    @Test
    fun `onDeleteServerItem llama markItemDeleted en repository`() = runTest {
        val list = fakeShoppingList(id = "slist-1")
        coEvery { getShoppingListUseCase("slist-1") } returns Result.Success(list)
        every { shoppingRepository.getCheckStates("slist-1") } returns flowOf(emptyMap())
        every { shoppingRepository.getLocalItems("slist-1") } returns flowOf(emptyList())
        every { shoppingRepository.getDeletedItemKeys("slist-1") } returns flowOf(emptySet())

        val vm = buildViewModel(listId = "slist-1")
        advanceUntilIdle()

        vm.onDeleteServerItem("Chicken", "PROTEINS")
        advanceUntilIdle()

        coVerify { shoppingRepository.markItemDeleted("slist-1", "Chicken", "PROTEINS") }
    }

    @Test
    fun `onRestoreServerItem llama unmarkItemDeleted en repository`() = runTest {
        val list = fakeShoppingList(id = "slist-1")
        coEvery { getShoppingListUseCase("slist-1") } returns Result.Success(list)
        every { shoppingRepository.getCheckStates("slist-1") } returns flowOf(emptyMap())
        every { shoppingRepository.getLocalItems("slist-1") } returns flowOf(emptyList())
        every { shoppingRepository.getDeletedItemKeys("slist-1") } returns flowOf(emptySet())

        val vm = buildViewModel(listId = "slist-1")
        advanceUntilIdle()

        vm.onRestoreServerItem("Chicken", "PROTEINS")
        advanceUntilIdle()

        coVerify { shoppingRepository.unmarkItemDeleted("slist-1", "Chicken", "PROTEINS") }
    }
}

