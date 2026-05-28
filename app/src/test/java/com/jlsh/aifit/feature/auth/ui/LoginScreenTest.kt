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
class LoginScreenTest {

    @get:Rule(order = 0)
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private val loginUseCase: LoginUseCase = mockk()
    private val registerUseCase: RegisterUseCase = mockk()
    private val googleUseCase: GoogleLoginUseCase = mockk()

    private fun createViewModel() = AuthViewModel(loginUseCase, registerUseCase, googleUseCase)

    @Test
    fun `pantalla muestra campo email en estado inicial`() {
        composeTestRule.setContent {
            AIFitTheme { LoginScreen(
                onNavigateToRegister = {},
                onNavigateToMain = {},
                onNavigateToCreateProfile = {},
                viewModel = createViewModel(),
            ) }
        }

        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
    }

    @Test
    fun `pantalla muestra campo contraseña en estado inicial`() {
        composeTestRule.setContent {
            AIFitTheme { LoginScreen(
                onNavigateToRegister = {},
                onNavigateToMain = {},
                onNavigateToCreateProfile = {},
                viewModel = createViewModel(),
            ) }
        }

        // The label of the password field according to strings.xml = "Password"
        composeTestRule.onNodeWithText("Contraseña").assertIsDisplayed()
    }

    @Test
    fun `pantalla muestra botón INICIAR SESIÓN`() {
        composeTestRule.setContent {
            AIFitTheme { LoginScreen(
                onNavigateToRegister = {},
                onNavigateToMain = {},
                onNavigateToCreateProfile = {},
                viewModel = createViewModel(),
            ) }
        }

        composeTestRule.onNodeWithText("INICIAR SESIÓN").assertIsDisplayed()
    }

    @Test
    fun `pantalla muestra separador o y botón CONTINUAR CON GOOGLE`() {
        composeTestRule.setContent {
            AIFitTheme { LoginScreen(
                onNavigateToRegister = {},
                onNavigateToMain = {},
                onNavigateToCreateProfile = {},
                viewModel = createViewModel(),
            ) }
        }

        composeTestRule.onNodeWithText("CONTINUAR CON GOOGLE", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `pulsar INICIAR SESIÓN con campos vacíos muestra error de email`() {
        composeTestRule.setContent {
            AIFitTheme { LoginScreen(
                onNavigateToRegister = {},
                onNavigateToMain = {},
                onNavigateToCreateProfile = {},
                viewModel = createViewModel(),
            ) }
        }

        composeTestRule.onNodeWithText("INICIAR SESIÓN").performClick()

        composeTestRule.onNodeWithText("El email es obligatorio").assertIsDisplayed()
    }

    @Test
    fun `pulsar INICIAR SESIÓN con email inválido muestra error de formato`() {
        val vm = createViewModel()
        composeTestRule.setContent {
            AIFitTheme { LoginScreen(
                onNavigateToRegister = {},
                onNavigateToMain = {},
                onNavigateToCreateProfile = {},
                viewModel = vm,
            ) }
        }

        composeTestRule.onNodeWithText("Email").performTextInput("no-es-email")
        composeTestRule.onNodeWithText("Contraseña").performTextInput("password123")
        composeTestRule.onNodeWithText("INICIAR SESIÓN").performClick()

        composeTestRule.onNodeWithText("Introduce un email válido").assertIsDisplayed()
    }

    @Test
    fun `pulsar Crear una navega a registro`() {
        var navigated = false
        composeTestRule.setContent {
            AIFitTheme { LoginScreen(
                onNavigateToRegister = { navigated = true },
                onNavigateToMain = {},
                onNavigateToCreateProfile = {},
                viewModel = createViewModel(),
            ) }
        }

        composeTestRule.onNodeWithText("Crear una").performScrollTo().performClick()

        assert(navigated)
    }

    @Test
    fun `login exitoso con profileComplete=true llama a onNavigateToMain`() {
        var navigated = false
        coEvery { loginUseCase(any(), any()) } returns Result.Success(fakeAuthToken(profileComplete = true))

        composeTestRule.setContent {
            AIFitTheme { LoginScreen(
                onNavigateToRegister = {},
                onNavigateToMain = { navigated = true },
                onNavigateToCreateProfile = {},
                viewModel = createViewModel(),
            ) }
        }

        composeTestRule.onNodeWithText("Email").performTextInput("test@aifit.com")
        composeTestRule.onNodeWithText("Contraseña").performTextInput("password123")
        composeTestRule.onNodeWithText("INICIAR SESIÓN").performClick()

        assert(navigated)
    }
}


