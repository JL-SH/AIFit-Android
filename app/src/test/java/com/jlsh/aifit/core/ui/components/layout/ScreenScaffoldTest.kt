package com.jlsh.aifit.core.ui.components.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ScreenScaffoldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ─── Minimal sealed UiState hierarchy for tests ────────────────────────

    private object TestLoadingState : UiStateHost.Loading

    private data class TestErrorState(override val message: String) : UiStateHost.Error

    private data class TestSuccessState(val label: String) : UiStateHost.Success

    // ─── Loading ───────────────────────────────────────────────────────────

    @Test
    fun `Loading state renders LoadingScreen with correct testTag`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                ScreenScaffold<TestSuccessState>(
                    uiState = TestLoadingState,
                    onRetry = {},
                ) { _, _ -> }
            }
        }

        composeTestRule.onNodeWithTag("loading_screen").assertIsDisplayed()
    }

    // ─── Error ─────────────────────────────────────────────────────────────

    @Test
    fun `Error state renders ErrorScreen with correct testTag`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                ScreenScaffold<TestSuccessState>(
                    uiState = TestErrorState("Sin conexión"),
                    onRetry = {},
                ) { _, _ -> }
            }
        }

        composeTestRule.onNodeWithTag("error_screen").assertIsDisplayed()
    }

    @Test
    fun `Error state displays the provided error message`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                ScreenScaffold<TestSuccessState>(
                    uiState = TestErrorState("Error del servidor"),
                    onRetry = {},
                ) { _, _ -> }
            }
        }

        composeTestRule.onNodeWithText("Error del servidor").assertIsDisplayed()
    }

    // ─── Success ───────────────────────────────────────────────────────────

    @Test
    fun `Success state invokes content lambda and renders its output`() {
        val successState = TestSuccessState(label = "Plan activo cargado")

        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                ScreenScaffold(
                    uiState = successState,
                    onRetry = {},
                ) { _: PaddingValues, state: TestSuccessState ->
                    Text(text = state.label)
                }
            }
        }

        composeTestRule.onNodeWithText("Plan activo cargado").assertIsDisplayed()
    }

    @Test
    fun `Success state does NOT show loading_screen`() {
        val successState = TestSuccessState(label = "OK")

        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                ScreenScaffold(
                    uiState = successState,
                    onRetry = {},
                ) { _: PaddingValues, state: TestSuccessState ->
                    Text(text = state.label)
                }
            }
        }

        composeTestRule.onNodeWithTag("loading_screen").assertDoesNotExist()
    }

    @Test
    fun `Loading state does NOT show error_screen`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                ScreenScaffold<TestSuccessState>(
                    uiState = TestLoadingState,
                    onRetry = {},
                ) { _, _ -> }
            }
        }

        composeTestRule.onNodeWithTag("error_screen").assertDoesNotExist()
    }
}

