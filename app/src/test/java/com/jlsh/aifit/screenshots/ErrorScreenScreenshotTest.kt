package com.jlsh.aifit.screenshots

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.jlsh.aifit.core.ui.components.feedback.ErrorScreen
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode


@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ErrorScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `ErrorScreen snapshot dark mode with network error message`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                ErrorScreen(
                    message = "Sin conexión. Comprueba tu internet.",
                    onRetry = {},
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/ErrorScreen_dark.png",
        )
    }

    @Test
    fun `ErrorScreen snapshot dark mode with server error message`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                ErrorScreen(
                    message = "Error del servidor. Inténtalo más tarde.",
                    onRetry = {},
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/ErrorScreen_server_dark.png",
        )
    }
}

