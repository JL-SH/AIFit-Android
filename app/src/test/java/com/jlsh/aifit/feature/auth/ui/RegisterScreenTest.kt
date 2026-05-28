package com.jlsh.aifit.feature.auth.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.feature.auth.domain.usecase.GoogleLoginUseCase
import com.jlsh.aifit.feature.auth.domain.usecase.LoginUseCase
import com.jlsh.aifit.feature.auth.domain.usecase.RegisterUseCase
import com.jlsh.aifit.testutil.MainDispatcherRule
import com.jlsh.aifit.testutil.fakeAuthToken
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class RegisterScreenTest {

    @get:Rule(order = 0)
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private val loginUseCase: LoginUseCase = mockk()
    private val registerUseCase: RegisterUseCase = mockk()
    private val googleUseCase: GoogleLoginUseCase = mockk()

    private fun createViewModel() = AuthViewModel(loginUseCase, registerUseCase, googleUseCase)

    @Test
    fun `pantalla muestra campo nombre en estado inicial`() {
        composeTestRule.setContent {
            AIFitTheme { RegisterScreen(
                onNavigateBack = {},
                onNavigateToCreateProfile = {},
                onNavigateToMain = {},
                viewModel = createViewModel(),
            ) }
        }

        composeTestRule.onNodeWithText("Nombre completo").assertIsDisplayed()
    }

    @Test
    fun `pantalla muestra campo email`() {
        composeTestRule.setContent {
            AIFitTheme { RegisterScreen(
                onNavigateBack = {},
                onNavigateToCreateProfile = {},
                onNavigateToMain = {},
                viewModel = createViewModel(),
            ) }
        }

        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
    }

    @Test
    fun `pantalla muestra campo contraseña`() {
        composeTestRule.setContent {
            AIFitTheme { RegisterScreen(
                onNavigateBack = {},
                onNavigateToCreateProfile = {},
                onNavigateToMain = {},
                viewModel = createViewModel(),
            ) }
        }

        composeTestRule.onNodeWithText("Contraseña").assertIsDisplayed()
    }

    @Test
    fun `pantalla muestra botón CREAR CUENTA`() {
        composeTestRule.setContent {
            AIFitTheme { RegisterScreen(
                onNavigateBack = {},
                onNavigateToCreateProfile = {},
                onNavigateToMain = {},
                viewModel = createViewModel(),
            ) }
        }

        composeTestRule.onNodeWithText("CREAR CUENTA", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `pantalla muestra enlace para iniciar sesión`() {
        composeTestRule.setContent {
            AIFitTheme { RegisterScreen(
                onNavigateBack = {},
                onNavigateToCreateProfile = {},
                onNavigateToMain = {},
                viewModel = createViewModel(),
            ) }
        }

        composeTestRule.onNodeWithText("Inicia sesión").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `pulsar CREAR CUENTA con campos vacíos muestra error`() {
        composeTestRule.setContent {
            AIFitTheme { RegisterScreen(
                onNavigateBack = {},
                onNavigateToCreateProfile = {},
                onNavigateToMain = {},
                viewModel = createViewModel(),
            ) }
        }

        composeTestRule.onNodeWithText("CREAR CUENTA", substring = true).performScrollTo().performClick()

        composeTestRule.onNodeWithText("El email es obligatorio").assertIsDisplayed()
    }

    @Test
    fun `registro exitoso llama a onNavigateToCreateProfile`() {
        var navigated = false
        coEvery { registerUseCase(any(), any(), any()) } returns
            Result.Success(fakeAuthToken(profileComplete = false))

        composeTestRule.setContent {
            AIFitTheme { RegisterScreen(
                onNavigateBack = {},
                onNavigateToCreateProfile = { navigated = true },
                onNavigateToMain = {},
                viewModel = createViewModel(),
            ) }
        }

        // Rellenar campos y enviar
        composeTestRule.onNodeWithText("Email").performTextInput("new@aifit.com")
        composeTestRule.onNodeWithText("Contraseña").performTextInput("password123")
        composeTestRule.onNodeWithText("Nombre completo").performTextInput("Test User")
        composeTestRule.onNodeWithText("CREAR CUENTA", substring = true).performScrollTo().performClick()

        assert(navigated)
    }
}


