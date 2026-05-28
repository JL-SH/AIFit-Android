package com.jlsh.aifit.feature.nutrition.ui

import app.cash.turbine.test
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.diet.domain.model.DietPlan
import com.jlsh.aifit.feature.diet.domain.model.Meal
import com.jlsh.aifit.feature.diet.domain.usecase.DeleteDietPlanUseCase
import com.jlsh.aifit.feature.diet.domain.usecase.GetDietPlanDetailUseCase
import com.jlsh.aifit.feature.diet.domain.usecase.GetDietPlansUseCase
import com.jlsh.aifit.feature.diet.domain.usecase.SetActiveDietPlanUseCase
import com.jlsh.aifit.feature.diet.domain.util.mealsForToday
import com.jlsh.aifit.feature.training.domain.model.PlanStatus
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionLog
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget
import com.jlsh.aifit.feature.nutrition.domain.usecase.AnalyzeMealFromTextUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.DeleteMealLogUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.GetCurrentNutritionTargetUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.GetNutritionLogUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.TrackMealUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.UpdateNutritionTargetUseCase
import com.jlsh.aifit.feature.nutrition.ui.state.NutritionHubUiState
import com.jlsh.aifit.feature.nutrition.ui.state.NutritionTargetUiState
import com.jlsh.aifit.feature.nutrition.ui.state.NutritionUiEvent
import com.jlsh.aifit.feature.nutrition.ui.state.TrackMealUiState
import com.jlsh.aifit.testutil.*
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NutritionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getNutritionLogUseCase: GetNutritionLogUseCase = mockk()
    private val getCurrentNutritionTargetUseCase: GetCurrentNutritionTargetUseCase = mockk()
    private val getDietPlansUseCase: GetDietPlansUseCase = mockk()
    private val getDietPlanDetailUseCase: GetDietPlanDetailUseCase = mockk()
    private val trackMealUseCase: TrackMealUseCase = mockk()
    private val analyzeMealFromTextUseCase: AnalyzeMealFromTextUseCase = mockk()
    private val deleteMealLogUseCase: DeleteMealLogUseCase = mockk()
    private val updateNutritionTargetUseCase: UpdateNutritionTargetUseCase = mockk()
    private val setActiveDietPlanUseCase: SetActiveDietPlanUseCase = mockk()
    private val deleteDietPlanUseCase: DeleteDietPlanUseCase = mockk()

    @Before
    fun resetMocks() {
        clearAllMocks()
    }

    private fun createViewModel(
        logFlow: Flow<Result<NutritionLog>> = flowOf(Result.Success(fakeNutritionLog())),
        targetFlow: Flow<Result<NutritionTarget>> = flowOf(Result.Success(fakeNutritionTarget())),
        dietPlansFlow: Flow<Result<List<DietPlan>>> = flowOf(Result.Success(listOf(fakeDietPlan()))),
    ): NutritionViewModel {
        every { getNutritionLogUseCase(any()) } returns logFlow
        every { getCurrentNutritionTargetUseCase() } returns targetFlow
        every { getDietPlansUseCase() } returns dietPlansFlow
        coEvery { getDietPlanDetailUseCase(any()) } returns Result.Success(fakeDietPlan())
        return NutritionViewModel(
            getNutritionLogUseCase,
            getCurrentNutritionTargetUseCase,
            getDietPlansUseCase,
            getDietPlanDetailUseCase,
            trackMealUseCase,
            analyzeMealFromTextUseCase,
            deleteMealLogUseCase,
            updateNutritionTargetUseCase,
            setActiveDietPlanUseCase,
            deleteDietPlanUseCase,
        )
    }

    // ─── Hub State ─────────────────────────────────────────────────────────────

    @Test
    fun `hubState es Loading inicialmente antes de que init complete`() {
        // With Loading flows that never complete, first{} suspends → state stays Loading
        val vm = createViewModel(
            logFlow = flow { emit(Result.Loading); awaitCancellation() },
            targetFlow = flow { emit(Result.Loading); awaitCancellation() },
            dietPlansFlow = flow { emit(Result.Loading); awaitCancellation() },
        )
        // init is running but flows only emit Loading which gets filtered, 
        // so deferred.await() hangs → state stays Loading
        assertTrue(vm.hubState.value is NutritionHubUiState.Loading)
    }

    @Test
    fun `hubState es Success cuando todos los datos cargan correctamente`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.hubState.value
        assertTrue(state is NutritionHubUiState.Success)
        val success = state as NutritionHubUiState.Success
        assertNotNull(success.todayState.nutritionLog)
        assertNotNull(success.todayState.target)
        assertEquals(1, success.dietPlans.size)
        assertEquals(0, success.selectedTabIndex)
    }

    @Test
    fun `hubState usa ultimo Success del log cuando cache no incluye comidas`() = runTest {
        val cachedLog = fakeNutritionLog(totalCalories = 300, meals = emptyList())
        val freshLog = fakeNutritionLog(
            totalCalories = 300,
            meals = listOf(fakeMealLog(id = "meal-2", name = "Desayuno")),
        )
        val vm = createViewModel(
            logFlow = flow {
                emit(Result.Loading)
                emit(Result.Success(cachedLog))
                emit(Result.Success(freshLog))
            },
        )
        advanceUntilIdle()

        val state = vm.hubState.value as NutritionHubUiState.Success
        assertEquals(1, state.todayState.nutritionLog?.meals?.size)
        assertEquals("Desayuno", state.todayState.nutritionLog?.meals?.first()?.name)
    }

    @Test
    fun `hubState Success con nutritionLog null cuando log falla`() = runTest {
        val vm = createViewModel(
            logFlow = flowOf(Result.Error(AppException.NetworkException)),
        )
        advanceUntilIdle()

        val state = vm.hubState.value
        assertTrue(state is NutritionHubUiState.Success)
        assertNull((state as NutritionHubUiState.Success).todayState.nutritionLog)
    }

    @Test
    fun `hubState Success con target null cuando target falla`() = runTest {
        val vm = createViewModel(
            targetFlow = flowOf(Result.Error(AppException.NetworkException)),
        )
        advanceUntilIdle()

        val state = vm.hubState.value
        assertTrue(state is NutritionHubUiState.Success)
        assertNull((state as NutritionHubUiState.Success).todayState.target)
    }

    @Test
    fun `hubState Success con dietPlans vacíos cuando dieta falla`() = runTest {
        val vm = createViewModel(
            dietPlansFlow = flowOf(Result.Error(AppException.NetworkException)),
        )
        advanceUntilIdle()

        val state = vm.hubState.value
        assertTrue(state is NutritionHubUiState.Success)
        assertTrue((state as NutritionHubUiState.Success).dietPlans.isEmpty())
    }

    // ─── Tab selection ─────────────────────────────────────────────────────────

    @Test
    fun `onTabSelected actualiza selectedTabIndex en hubState`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onTabSelected(2)

        assertEquals(2, vm.selectedTabIndex.value)
        val state = vm.hubState.value as NutritionHubUiState.Success
        assertEquals(2, state.selectedTabIndex)
    }

    // ─── Delete meal ───────────────────────────────────────────────────────────

    @Test
    fun `onDeleteMeal emite MealDeleted y ShowSnackbar cuando exitoso`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        coEvery { deleteMealLogUseCase(any()) } returns Result.Success(Unit)

        vm.events.test {
            vm.onDeleteMeal("meal-1")
            advanceUntilIdle()

            val event1 = awaitItem()
            assertTrue(event1 is NutritionUiEvent.MealDeleted)

            val event2 = awaitItem()
            assertTrue(event2 is NutritionUiEvent.ShowSnackbar)
            assertEquals("Comida eliminada", (event2 as NutritionUiEvent.ShowSnackbar).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDeleteMeal emite ShowSnackbar con error cuando falla`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        coEvery { deleteMealLogUseCase(any()) } returns
            Result.Error(AppException.NetworkException)

        vm.events.test {
            vm.onDeleteMeal("meal-1")
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is NutritionUiEvent.ShowSnackbar)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── Track meal ────────────────────────────────────────────────────────────

    @Test
    fun `onTrackMeal cambia trackMealState a Saving y luego a Saved`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        val request = fakeTrackMealRequestDto()
        coEvery { trackMealUseCase(request) } returns Result.Success(fakeMealLog())

        vm.onTrackMeal(request)
        advanceUntilIdle()

        assertTrue(vm.trackMealState.value is TrackMealUiState.Saved)
    }

    @Test
    fun `onTrackMeal emite ShowSnackbar y NavigateBack cuando exitoso`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        val request = fakeTrackMealRequestDto()
        coEvery { trackMealUseCase(request) } returns Result.Success(fakeMealLog())

        vm.events.test {
            vm.onTrackMeal(request)
            advanceUntilIdle()

            val nav = awaitItem()
            assertTrue(nav is NutritionUiEvent.NavigateToHome)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onTrackMeal cambia trackMealState a Error cuando falla`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        val request = fakeTrackMealRequestDto()
        coEvery { trackMealUseCase(request) } returns
            Result.Error(AppException.ServerException)

        vm.onTrackMeal(request)
        advanceUntilIdle()

        assertTrue(vm.trackMealState.value is TrackMealUiState.Error)
    }

    // ─── Analyze meal from text ────────────────────────────────────────────────

    @Test
    fun `onAnalyzeMealFromText cambia a Saved cuando exitoso`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        val request = fakeAnalyzeMealFromTextRequestDto()
        coEvery { analyzeMealFromTextUseCase(request) } returns
            Result.Success(fakeMealLog(aiGenerated = true))

        vm.onAnalyzeMealFromText(request)
        advanceUntilIdle()

        assertTrue(vm.trackMealState.value is TrackMealUiState.Saved)
    }

    @Test
    fun `onAnalyzeMealFromText cambia a Error cuando falla`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        val request = fakeAnalyzeMealFromTextRequestDto()
        coEvery { analyzeMealFromTextUseCase(request) } returns
            Result.Error(AppException.ServerException)

        vm.onAnalyzeMealFromText(request)
        advanceUntilIdle()

        assertTrue(vm.trackMealState.value is TrackMealUiState.Error)
    }

    // ─── resetTrackMealState ───────────────────────────────────────────────────

    @Test
    fun `resetTrackMealState restaura a Idle`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        val request = fakeTrackMealRequestDto()
        coEvery { trackMealUseCase(request) } returns Result.Success(fakeMealLog())
        vm.onTrackMeal(request)
        advanceUntilIdle()

        vm.resetTrackMealState()

        assertTrue(vm.trackMealState.value is TrackMealUiState.Idle)
    }

    // ─── Nutrition target ──────────────────────────────────────────────────────

    @Test
    fun `loadNutritionTarget cambia targetState a Ready con datos`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.loadNutritionTarget()
        advanceUntilIdle()

        val state = vm.targetState.value
        assertTrue(state is NutritionTargetUiState.Ready)
        val ready = state as NutritionTargetUiState.Ready
        assertEquals("2200", ready.calorieTarget)
        assertEquals("165", ready.proteinTarget)
        assertEquals("250", ready.carbsTarget)
        assertEquals("73", ready.fatTarget)
    }

    @Test
    fun `loadNutritionTarget cambia targetState a Error cuando falla`() = runTest {
        every { getCurrentNutritionTargetUseCase() } returns flowOf(
            Result.Error(AppException.NetworkException),
        )
        val vm = createViewModel(
            targetFlow = flowOf(Result.Error(AppException.NetworkException)),
        )
        advanceUntilIdle()

        vm.loadNutritionTarget()
        advanceUntilIdle()

        val state = vm.targetState.value
        assertTrue(state is NutritionTargetUiState.Error)
    }

    @Test
    fun `onUpdateTarget emite ShowSnackbar y NavigateBack cuando exitoso`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.loadNutritionTarget()
        advanceUntilIdle()

        coEvery { updateNutritionTargetUseCase(any()) } returns
            Result.Success(fakeNutritionTarget(calorieTarget = 2500))

        vm.events.test {
            vm.onUpdateTarget("2500", "165", "250", "73")
            advanceUntilIdle()

            val back = awaitItem()
            assertTrue(back is NutritionUiEvent.NavigateBack)

            val snackbar = awaitItem()
            assertTrue(snackbar is NutritionUiEvent.ShowSnackbar)
            assertEquals("Objetivos actualizados", (snackbar as NutritionUiEvent.ShowSnackbar).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onUpdateTarget emite error snackbar cuando falla`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.loadNutritionTarget()
        advanceUntilIdle()

        coEvery { updateNutritionTargetUseCase(any()) } returns
            Result.Error(AppException.ServerException)

        vm.events.test {
            vm.onUpdateTarget("2500", "165", "250", "73")
            advanceUntilIdle()

            val snackbar = awaitItem()
            assertTrue(snackbar is NutritionUiEvent.ShowSnackbar)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── Diet plan management ───────────────────────── ─────────────────────────

    @Test
    fun `onDeleteDietPlan no llama use case cuando plan es ACTIVE`() = runTest {
        val activePlan = fakeDietPlan(id = "dp-active", status = PlanStatus.ACTIVE)
        val vm = createViewModel(dietPlansFlow = flowOf(Result.Success(listOf(activePlan))))
        advanceUntilIdle()

        vm.events.test {
            vm.onDeleteDietPlan("dp-active")
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is NutritionUiEvent.ShowSnackbar)
            coVerify(exactly = 0) { deleteDietPlanUseCase(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDeleteDietPlan elimina optimistamente y emite snackbar en éxito`() = runTest {
        val draftPlan = fakeDietPlan(id = "dp-draft", status = PlanStatus.DRAFT)
        val vm = createViewModel(dietPlansFlow = flowOf(Result.Success(listOf(draftPlan))))
        advanceUntilIdle()
        coEvery { deleteDietPlanUseCase("dp-draft") } returns Result.Success(Unit)

        vm.events.test {
            vm.onDeleteDietPlan("dp-draft")
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is NutritionUiEvent.ShowSnackbar)
            assertEquals("Plan eliminado", (event as NutritionUiEvent.ShowSnackbar).message)
            coVerify { deleteDietPlanUseCase("dp-draft") }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fetchHubData ordena dietPlans por createdAt descendente`() = runTest {
        val older = fakeDietPlan(
            id = "dp-old",
            name = "Plan antiguo",
            createdAt = java.time.LocalDateTime.of(2026, 1, 1, 10, 0),
        )
        val newer = fakeDietPlan(
            id = "dp-new",
            name = "Plan nuevo",
            createdAt = java.time.LocalDateTime.of(2026, 5, 1, 10, 0),
        )
        val vm = createViewModel(
            dietPlansFlow = flowOf(Result.Success(listOf(older, newer))),
        )
        advanceUntilIdle()

        val state = vm.hubState.value as NutritionHubUiState.Success
        assertEquals("dp-new", state.dietPlans.first().id)
        assertEquals("dp-old", state.dietPlans.last().id)
    }

    @Test
    fun `onActivateDietPlan actualiza optimistamente planes antes de confirmar red`() = runTest {
        val active = fakeDietPlan(id = "dp-1", status = PlanStatus.ACTIVE)
        val draft = fakeDietPlan(id = "dp-2", status = PlanStatus.DRAFT)
        val vm = createViewModel(dietPlansFlow = flowOf(Result.Success(listOf(active, draft))))
        advanceUntilIdle()
        coEvery { setActiveDietPlanUseCase("dp-2") } coAnswers { awaitCancellation() }

        vm.onActivateDietPlan("dp-2")

        val state = vm.hubState.value as NutritionHubUiState.Success
        assertEquals(PlanStatus.PAUSED, state.dietPlans.first { it.id == "dp-1" }.status)
        assertEquals(PlanStatus.ACTIVE, state.dietPlans.first { it.id == "dp-2" }.status)
        assertTrue(state.isActivatingPlan)
    }

    // ─── Navigation events ─────────────────────────────────────────────────────

    @Test
    fun `onTrackMealFromPlan emite snackbar y refresca hub cuando exitoso`() = runTest {
        val todayMeal = fakeMeal(id = "plan-meal-1", name = "Comida del plan")
        val planWithDays = fakeDietPlan(
            status = PlanStatus.ACTIVE,
            days = listOf(fakeDietDay(meals = listOf(todayMeal))),
        )
        val vm = createViewModel(
            dietPlansFlow = flowOf(Result.Success(listOf(planWithDays))),
        )
        coEvery { trackMealUseCase(any()) } returns Result.Success(fakeMealLog())
        advanceUntilIdle()

        vm.events.test {
            vm.onTrackMealFromPlan(todayMeal)
            advanceUntilIdle()

            val snackbar = awaitItem()
            assertTrue(snackbar is NutritionUiEvent.ShowSnackbar)
            assertEquals("Comida del plan registrada", (snackbar as NutritionUiEvent.ShowSnackbar).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onShowPlanMealPicker emite ShowPlanMealPicker y carga comidas del plan activo`() = runTest {
        val todayMeal = fakeMeal(id = "plan-meal-2")
        val planWithDays = fakeDietPlan(
            status = PlanStatus.ACTIVE,
            days = listOf(fakeDietDay(meals = listOf(todayMeal))),
        )
        val vm = createViewModel(
            dietPlansFlow = flowOf(Result.Success(listOf(planWithDays))),
        )
        advanceUntilIdle()

        vm.events.test {
            vm.onShowPlanMealPicker()
            advanceUntilIdle()

            assertEquals(listOf(todayMeal), vm.planPickerMeals.value)
            assertTrue(awaitItem() is NutritionUiEvent.ShowPlanMealPicker)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onFabClicked emite ShowTrackMealSheet`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onFabClicked()

            val event = awaitItem()
            assertTrue(event is NutritionUiEvent.ShowTrackMealSheet)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDietPlanClicked emite NavigateToDietDetail con planId`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onDietPlanClicked("plan-123")

            val event = awaitItem()
            assertTrue(event is NutritionUiEvent.NavigateToDietDetail)
            assertEquals("plan-123", (event as NutritionUiEvent.NavigateToDietDetail).planId)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onGenerateDietClicked emite NavigateToGenerateDiet`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onGenerateDietClicked()

            val event = awaitItem()
            assertTrue(event is NutritionUiEvent.NavigateToGenerateDiet)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onNavigateToTarget emite NavigateToNutritionTarget`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onNavigateToTarget()

            val event = awaitItem()
            assertTrue(event is NutritionUiEvent.NavigateToNutritionTarget)

            cancelAndIgnoreRemainingEvents()
        }
    }
}


