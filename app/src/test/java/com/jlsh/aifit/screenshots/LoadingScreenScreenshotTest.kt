package com.jlsh.aifit.screenshots

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.jlsh.aifit.core.ui.components.feedback.LoadingScreen
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LoadingScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `LoadingScreen snapshot dark mode`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                LoadingScreen()
            }
        }

        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/LoadingScreen_dark.png",
        )
    }
}

