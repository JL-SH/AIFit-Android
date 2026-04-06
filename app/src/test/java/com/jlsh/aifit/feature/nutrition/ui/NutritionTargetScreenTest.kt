package com.jlsh.aifit.feature.nutrition.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.diet.domain.usecase.GetDietPlansUseCase
import com.jlsh.aifit.feature.nutrition.domain.model.NutritionTarget
import com.jlsh.aifit.feature.nutrition.domain.usecase.GetCurrentNutritionTargetUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.GetNutritionLogUseCase
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.testutil.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class NutritionTargetScreenTest {

    @get:Rule(order = 0)
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private fun createViewModel(
        targetFlow: Flow<Result<NutritionTarget>> = flowOf(Result.Success(fakeNutritionTarget())),
    ): NutritionViewModel {
        val getNutritionLog: GetNutritionLogUseCase = mockk()
        val getTarget: GetCurrentNutritionTargetUseCase = mockk()
        val getDietPlans: GetDietPlansUseCase = mockk()

        every { getNutritionLog(any()) } returns flowOf(
            Result.Success(fakeNutritionLog()),
        )
        every { getTarget() } returns targetFlow
        every { getDietPlans() } returns flowOf(Result.Success(emptyList()))

        return NutritionViewModel(
            getNutritionLogUseCase = getNutritionLog,
            getCurrentNutritionTargetUseCase = getTarget,
            getDietPlansUseCase = getDietPlans,
            trackMealUseCase = mockk(),
            analyzeMealFromTextUseCase = mockk(),
            deleteMealLogUseCase = mockk(),
            updateNutritionTargetUseCase = mockk(),
        )
    }

    private fun setScreen(vm: NutritionViewModel) {
        composeTestRule.setContent {
            AIFitTheme {
                NutritionTargetScreen(
                    onNavigateBack = {},
                    viewModel = vm,
                )
            }
        }
    }

    @Test
    fun `muestra LoadingScreen cuando targetState es Loading`() {
        // target returns only Loading so targetState stays Loading
        val vm = createViewModel(targetFlow = flow { emit(Result.Loading); awaitCancellation() })
        setScreen(vm)

        composeTestRule.onNodeWithTag("loading_screen").assertIsDisplayed()
    }

    @Test
    fun `muestra ErrorScreen cuando target falla`() {
        val vm = createViewModel(
            targetFlow = flowOf(Result.Error(AppException.NetworkException)),
        )
        setScreen(vm)

        composeTestRule.onNodeWithTag("error_screen").assertIsDisplayed()
    }

    @Test
    fun `muestra campos de target cuando targetState es Ready`() {
        val vm = createViewModel()
        setScreen(vm)

        composeTestRule.onNodeWithText("Nutrition Targets").assertIsDisplayed()
        composeTestRule.onNodeWithText("SAVE").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `muestra chip setBy MANUAL cuando target es manual`() {
        val vm = createViewModel()
        setScreen(vm)

        composeTestRule.onNodeWithText("MANUAL").assertIsDisplayed()
    }
}


