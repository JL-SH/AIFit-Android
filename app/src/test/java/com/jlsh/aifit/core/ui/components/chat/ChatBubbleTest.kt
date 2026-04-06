package com.jlsh.aifit.core.ui.components.chat

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ChatBubbleTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `user bubble displays content text`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                ChatBubble(
                    content = "¿Puedo cambiar el plan?",
                    isUser = true,
                    timestamp = "10:30",
                    isMarkdown = false,
                )
            }
        }

        composeTestRule.onNodeWithText("¿Puedo cambiar el plan?").assertIsDisplayed()
    }

    @Test
    fun `assistant bubble displays content text`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                ChatBubble(
                    content = "Claro, podemos ajustar tu plan.",
                    isUser = false,
                    timestamp = "10:31",
                    isMarkdown = false,
                )
            }
        }

        composeTestRule.onNodeWithText("Claro, podemos ajustar tu plan.").assertIsDisplayed()
    }

    @Test
    fun `timestamp is displayed below the bubble`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                ChatBubble(
                    content = "Mensaje de prueba",
                    isUser = true,
                    timestamp = "14:55",
                    isMarkdown = false,
                )
            }
        }

        composeTestRule.onNodeWithText("14:55").assertIsDisplayed()
    }

    @Test
    fun `user and assistant bubbles render without crash when rendered together`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                ChatBubble(content = "Hola", isUser = true, timestamp = "09:00")
                ChatBubble(content = "¡Hola! ¿En qué puedo ayudarte?", isUser = false, timestamp = "09:01")
            }
        }

        composeTestRule.onNodeWithText("Hola").assertIsDisplayed()
        composeTestRule.onNodeWithText("¡Hola! ¿En qué puedo ayudarte?").assertIsDisplayed()
    }
}

