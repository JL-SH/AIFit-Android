package com.jlsh.aifit.core.ui.components.inputs

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AiFitTextFieldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `label is displayed`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                AiFitTextField(value = "", onValueChange = {}, label = "Email")
            }
        }

        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
    }

    @Test
    fun `error supporting text is displayed when error is non-null`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                AiFitTextField(
                    value = "bad-email",
                    onValueChange = {},
                    label = "Email",
                    error = "Email inválido",
                )
            }
        }

        composeTestRule.onNodeWithText("Email inválido").assertIsDisplayed()
    }

    @Test
    fun `error supporting text is NOT shown when error is null`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                AiFitTextField(
                    value = "user@aifit.com",
                    onValueChange = {},
                    label = "Email",
                    error = null,
                )
            }
        }

        composeTestRule.onNodeWithText("Email inválido").assertDoesNotExist()
    }

    @Test
    fun `field value is displayed`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                AiFitTextField(
                    value = "user@aifit.com",
                    onValueChange = {},
                    label = "Email",
                )
            }
        }

        composeTestRule.onNodeWithText("user@aifit.com").assertIsDisplayed()
    }
}

