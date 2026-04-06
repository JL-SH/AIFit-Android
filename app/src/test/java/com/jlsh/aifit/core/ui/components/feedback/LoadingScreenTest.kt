package com.jlsh.aifit.core.ui.components.feedback

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LoadingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `loading_screen testTag is present and displayed`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                LoadingScreen()
            }
        }

        composeTestRule
            .onNodeWithTag("loading_screen")
            .assertIsDisplayed()
    }
}

