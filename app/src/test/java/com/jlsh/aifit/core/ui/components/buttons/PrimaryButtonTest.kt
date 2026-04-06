package com.jlsh.aifit.core.ui.components.buttons

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PrimaryButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `text is displayed when isLoading is false`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                PrimaryButton(text = "Guardar", onClick = {})
            }
        }

        // PrimaryButton renders text.uppercase()
        composeTestRule.onNodeWithText("GUARDAR").assertIsDisplayed()
    }

    @Test
    fun `text is NOT displayed when isLoading is true`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                PrimaryButton(text = "Guardar", onClick = {}, isLoading = true)
            }
        }

        // When loading, only CircularProgressIndicator is shown — no text node
        composeTestRule.onNodeWithText("GUARDAR").assertDoesNotExist()
    }

    @Test
    fun `onClick is invoked when button is clicked`() {
        var clicked = false
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                PrimaryButton(text = "Continuar", onClick = { clicked = true })
            }
        }

        composeTestRule.onNodeWithText("CONTINUAR").performClick()

        assertTrue("onClick must be called on button click", clicked)
    }

    @Test
    fun `onClick is NOT invoked when isLoading is true`() {
        var clicked = false
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                PrimaryButton(text = "Continuar", onClick = { clicked = true }, isLoading = true)
            }
        }

        // Button is disabled when loading — click should have no effect
        composeTestRule.onNodeWithText("CONTINUAR").assertDoesNotExist()
        assertFalse("onClick must NOT be called while loading", clicked)
    }

    @Test
    fun `button is not enabled when enabled=false`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                PrimaryButton(text = "Guardar", onClick = {}, enabled = false)
            }
        }

        composeTestRule.onNodeWithText("GUARDAR").assertIsNotEnabled()
    }
}

