package com.jlsh.aifit.core.ui.components.feedback

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ErrorScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `error_screen testTag is present and displayed`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                ErrorScreen(message = "Sin conexión.", onRetry = {})
            }
        }

        composeTestRule.onNodeWithTag("error_screen").assertIsDisplayed()
    }

    @Test
    fun `error message text is displayed`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                ErrorScreen(message = "Error de red. Inténtalo de nuevo.", onRetry = {})
            }
        }

        composeTestRule
            .onNodeWithText("Error de red. Inténtalo de nuevo.")
            .assertIsDisplayed()
    }

    @Test
    fun `retry button click invokes onRetry callback`() {
        var retryCalled = false

        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                ErrorScreen(
                    message = "Error de conexión.",
                    onRetry = { retryCalled = true },
                )
            }
        }

        // "Reintentar" is the value of R.string.common_retry
        composeTestRule.onNodeWithText("Reintentar", ignoreCase = true).performClick()

        assertTrue("onRetry callback must be invoked", retryCalled)
    }

    @Test
    fun `different error messages are displayed correctly`() {
        val message = "Error del servidor. Inténtalo más tarde."
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                ErrorScreen(message = message, onRetry = {})
            }
        }

        composeTestRule.onNodeWithText(message).assertIsDisplayed()
    }
}

