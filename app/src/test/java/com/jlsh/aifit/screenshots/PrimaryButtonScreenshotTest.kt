package com.jlsh.aifit.screenshots

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PrimaryButtonScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `PrimaryButton snapshot dark mode default state`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                PrimaryButton(text = "Guardar plan", onClick = {})
            }
        }

        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/PrimaryButton_default_dark.png",
        )
    }

    @Test
    fun `PrimaryButton snapshot dark mode loading state`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                PrimaryButton(text = "Guardando...", onClick = {}, isLoading = true)
            }
        }

        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/PrimaryButton_loading_dark.png",
        )
    }

    @Test
    fun `PrimaryButton snapshot dark mode disabled state`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                PrimaryButton(text = "Guardar plan", onClick = {}, enabled = false)
            }
        }

        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/PrimaryButton_disabled_dark.png",
        )
    }
}

