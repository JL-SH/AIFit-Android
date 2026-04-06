package com.jlsh.aifit.core.ui.components.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CheckableListItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `item text is displayed`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                CheckableListItem(
                    text = "Press banca",
                    checked = false,
                    onCheckedChange = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Press banca").assertIsDisplayed()
    }

    @Test
    fun `subtitle is displayed when provided`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                CheckableListItem(
                    text = "Press banca",
                    checked = false,
                    onCheckedChange = {},
                    subtitle = "4 series — 8 reps",
                )
            }
        }

        composeTestRule.onNodeWithText("4 series — 8 reps").assertIsDisplayed()
    }

    @Test
    fun `subtitle is NOT displayed when null`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                CheckableListItem(
                    text = "Press banca",
                    checked = false,
                    onCheckedChange = {},
                    subtitle = null,
                )
            }
        }

        composeTestRule.onNodeWithText("4 series — 8 reps").assertDoesNotExist()
    }

    @Test
    fun `clicking the row invokes onCheckedChange with toggled value`() {
        var newCheckedValue: Boolean? = null

        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                CheckableListItem(
                    text = "Sentadillas",
                    checked = false,
                    onCheckedChange = { newCheckedValue = it },
                )
            }
        }

        composeTestRule.onNodeWithText("Sentadillas").performClick()

        assertTrue(
            "Clicking unchecked item should call onCheckedChange(true)",
            newCheckedValue == true,
        )
    }

    @Test
    fun `clicking a checked row calls onCheckedChange with false`() {
        var newCheckedValue: Boolean? = null

        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                CheckableListItem(
                    text = "Sentadillas",
                    checked = true,
                    onCheckedChange = { newCheckedValue = it },
                )
            }
        }

        composeTestRule.onNodeWithText("Sentadillas").performClick()

        assertTrue(
            "Clicking checked item should call onCheckedChange(false)",
            newCheckedValue == false,
        )
    }
}

