package com.jlsh.aifit.screenshots

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.feature.diet.domain.usecase.GetDietPlansUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.GetCurrentNutritionTargetUseCase
import com.jlsh.aifit.feature.nutrition.domain.usecase.GetNutritionLogUseCase
import com.jlsh.aifit.feature.nutrition.ui.NutritionHubScreen
import com.jlsh.aifit.feature.nutrition.ui.NutritionTargetScreen
import com.jlsh.aifit.feature.nutrition.ui.NutritionViewModel
import com.jlsh.aifit.testutil.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class NutritionScreenshotTest {

    @get:Rule(order = 0)
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private fun createViewModel(): NutritionViewModel {
        val getNutritionLog: GetNutritionLogUseCase = mockk()
        val getTarget: GetCurrentNutritionTargetUseCase = mockk()
        val getDietPlans: GetDietPlansUseCase = mockk()

        every { getNutritionLog(any()) } returns flowOf(Result.Success(fakeNutritionLog()))
        every { getTarget() } returns flowOf(Result.Success(fakeNutritionTarget()))
        every { getDietPlans() } returns flowOf(Result.Success(listOf(fakeDietPlan())))

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

    @Test
    fun `NutritionHubScreen snapshot estado Success dark mode`() {
        val vm = createViewModel()
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                NutritionHubScreen(
                    onNavigateToTrackMeal = {},
                    onNavigateToFoodVision = {},
                    onNavigateToNutritionTarget = {},
                    onNavigateToDietDetail = {},
                    onNavigateToGenerateDiet = {},
                    onNavigateToShoppingDetail = {},
                    viewModel = vm,
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/NutritionHubScreen_success_dark.png",
        )
    }

    @Test
    fun `NutritionHubScreen snapshot estado Loading dark mode`() {
        val getNutritionLog: GetNutritionLogUseCase = mockk()
        val getTarget: GetCurrentNutritionTargetUseCase = mockk()
        val getDietPlans: GetDietPlansUseCase = mockk()

        every { getNutritionLog(any()) } returns flow { emit(Result.Loading); awaitCancellation() }
        every { getTarget() } returns flow { emit(Result.Loading); awaitCancellation() }
        every { getDietPlans() } returns flow { emit(Result.Loading); awaitCancellation() }

        val vm = NutritionViewModel(
            getNutritionLogUseCase = getNutritionLog,
            getCurrentNutritionTargetUseCase = getTarget,
            getDietPlansUseCase = getDietPlans,
            trackMealUseCase = mockk(),
            analyzeMealFromTextUseCase = mockk(),
            deleteMealLogUseCase = mockk(),
            updateNutritionTargetUseCase = mockk(),
        )

        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                NutritionHubScreen(
                    onNavigateToTrackMeal = {},
                    onNavigateToFoodVision = {},
                    onNavigateToNutritionTarget = {},
                    onNavigateToDietDetail = {},
                    onNavigateToGenerateDiet = {},
                    onNavigateToShoppingDetail = {},
                    viewModel = vm,
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/NutritionHubScreen_loading_dark.png",
        )
    }

    @Test
    fun `NutritionTargetScreen snapshot estado Ready dark mode`() {
        val vm = createViewModel()
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                NutritionTargetScreen(
                    onNavigateBack = {},
                    viewModel = vm,
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/NutritionTargetScreen_ready_dark.png",
        )
    }
}


