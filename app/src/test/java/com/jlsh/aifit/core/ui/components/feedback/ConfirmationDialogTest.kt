package com.jlsh.aifit.core.ui.components.feedback

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
class ConfirmationDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun showDialog(
        title: String = "Eliminar plan",
        message: String = "Esta acción no se puede deshacer.",
        onConfirm: () -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                ConfirmationDialog(
                    title = title,
                    message = message,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss,
                    confirmText = "Confirmar",
                    dismissText = "Cancelar",
                )
            }
        }
    }

    @Test
    fun `dialog title is displayed`() {
        showDialog(title = "Borrar sesión")
        composeTestRule.onNodeWithText("Borrar sesión").assertIsDisplayed()
    }

    @Test
    fun `dialog message is displayed`() {
        showDialog(message = "¿Estás seguro?")
        composeTestRule.onNodeWithText("¿Estás seguro?").assertIsDisplayed()
    }

    @Test
    fun `clicking Confirmar invokes onConfirm callback`() {
        var confirmCalled = false
        showDialog(onConfirm = { confirmCalled = true })

        composeTestRule.onNodeWithText("CONFIRMAR", ignoreCase = true).performClick()

        assertTrue("onConfirm must be called when confirm button is clicked", confirmCalled)
    }

    @Test
    fun `clicking Cancelar invokes onDismiss callback`() {
        var dismissCalled = false
        showDialog(onDismiss = { dismissCalled = true })

        composeTestRule.onNodeWithText("CANCELAR", ignoreCase = true).performClick()

        assertTrue("onDismiss must be called when dismiss button is clicked", dismissCalled)
    }

    @Test
    fun `both title and message are visible at the same time`() {
        showDialog(title = "Cerrar sesión", message = "Se cerrará tu sesión actual.")

        composeTestRule.onNodeWithText("Cerrar sesión").assertIsDisplayed()
        composeTestRule.onNodeWithText("Se cerrará tu sesión actual.").assertIsDisplayed()
    }
}

