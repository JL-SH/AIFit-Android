package com.jlsh.aifit.core.ui.components.display

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PlanStatusBadgeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun renderBadge(status: String) {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                PlanStatusBadge(status = status)
            }
        }
    }

    @Test
    fun `ACTIVE status is displayed uppercase`() {
        renderBadge("ACTIVE")
        composeTestRule.onNodeWithText("ACTIVE").assertIsDisplayed()
    }

    @Test
    fun `lowercase active input is uppercased before display`() {
        renderBadge("active")
        composeTestRule.onNodeWithText("ACTIVE").assertIsDisplayed()
    }

    @Test
    fun `COMPLETED status is displayed uppercase`() {
        renderBadge("COMPLETED")
        composeTestRule.onNodeWithText("COMPLETED").assertIsDisplayed()
    }

    @Test
    fun `DRAFT status is displayed uppercase`() {
        renderBadge("DRAFT")
        composeTestRule.onNodeWithText("DRAFT").assertIsDisplayed()
    }

    @Test
    fun `ARCHIVED status is displayed uppercase`() {
        renderBadge("ARCHIVED")
        composeTestRule.onNodeWithText("ARCHIVED").assertIsDisplayed()
    }

    @Test
    fun `unknown status renders without crash`() {
        // Should not throw; falls through to the else branch in statusColors
        renderBadge("PAUSED")
        composeTestRule.onNodeWithText("PAUSED").assertIsDisplayed()
    }
}

