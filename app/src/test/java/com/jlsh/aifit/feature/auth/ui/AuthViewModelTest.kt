package com.jlsh.aifit.feature.auth.ui

import app.cash.turbine.test
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.auth.domain.usecase.GoogleLoginUseCase
import com.jlsh.aifit.feature.auth.domain.usecase.LoginUseCase
import com.jlsh.aifit.feature.auth.domain.usecase.RegisterUseCase
import com.jlsh.aifit.feature.auth.ui.state.AuthUiEvent
import com.jlsh.aifit.feature.auth.ui.state.AuthUiState
import com.jlsh.aifit.testutil.MainDispatcherRule
import com.jlsh.aifit.testutil.fakeAuthToken
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val loginUseCase: LoginUseCase = mockk()
    private val registerUseCase: RegisterUseCase = mockk()
    private val googleLoginUseCase: GoogleLoginUseCase = mockk()

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        viewModel = AuthViewModel(loginUseCase, registerUseCase, googleLoginUseCase)
    }

    // ─── Estado inicial ────────────────────────────────────────────────────────

    @Test
    fun `estado inicial es Idle`() = runTest {
        assertTrue(viewModel.uiState.value is AuthUiState.Idle)
    }

    @Test
    fun `campos iniciales estan vacios y sin errores`() = runTest {
        assertEquals("", viewModel.email.value)
        assertEquals("", viewModel.password.value)
        assertEquals("", viewModel.name.value)
        assertNull(viewModel.emailError.value)
        assertNull(viewModel.passwordError.value)
        assertNull(viewModel.nameError.value)
    }

    // ─── onFieldChanged ────────────────────────────────────────────────────────

    @Test
    fun `onEmailChanged actualiza email y limpia emailError`() = runTest {
        viewModel.onLoginClicked() // genera error en email vacio
        viewModel.onEmailChanged("nuevo@email.com")

        assertEquals("nuevo@email.com", viewModel.email.value)
        assertNull(viewModel.emailError.value)
    }

    @Test
    fun `onPasswordChanged actualiza password y limpia passwordError`() = runTest {
        viewModel.onLoginClicked() // genera error en password vacia
        viewModel.onPasswordChanged("nuevaPass")

        assertEquals("nuevaPass", viewModel.password.value)
        assertNull(viewModel.passwordError.value)
    }

    @Test
    fun `onNameChanged actualiza name y limpia nameError`() = runTest {
        viewModel.onRegisterClicked() // genera error en nombre vacio
        viewModel.onNameChanged("Juan")

        assertEquals("Juan", viewModel.name.value)
        assertNull(viewModel.nameError.value)
    }

    // ─── Validacion Login ──────────────────────────────────────────────────────

    @Test
    fun `onLoginClicked con email vacio setea emailError y no llama al useCase`() = runTest {
        viewModel.onPasswordChanged("password123")

        viewModel.onLoginClicked()

        assertEquals("El email es obligatorio", viewModel.emailError.value)
        assertTrue(viewModel.uiState.value is AuthUiState.Idle)
    }

    @Test
    fun `onLoginClicked con email invalido setea emailError`() = runTest {
        viewModel.onEmailChanged("no-es-email")
        viewModel.onPasswordChanged("password123")

        viewModel.onLoginClicked()

        assertEquals("Introduce un email válido", viewModel.emailError.value)
    }

    @Test
    fun `onLoginClicked con password vacia setea passwordError`() = runTest {
        viewModel.onEmailChanged("test@aifit.com")

        viewModel.onLoginClicked()

        assertEquals("La contraseña es obligatoria", viewModel.passwordError.value)
        assertTrue(viewModel.uiState.value is AuthUiState.Idle)
    }

    @Test
    fun `onLoginClicked con password menor de 6 caracteres setea passwordError`() = runTest {
        viewModel.onEmailChanged("test@aifit.com")
        viewModel.onPasswordChanged("abc")

        viewModel.onLoginClicked()

        assertEquals("Mínimo 6 caracteres", viewModel.passwordError.value)
    }

    // ─── Validacion Register ───────────────────────────────────────────────────

    @Test
    fun `onRegisterClicked con nombre vacio setea nameError`() = runTest {
        viewModel.onEmailChanged("test@aifit.com")
        viewModel.onPasswordChanged("password123")

        viewModel.onRegisterClicked()

        assertEquals("El nombre es obligatorio", viewModel.nameError.value)
        assertTrue(viewModel.uiState.value is AuthUiState.Idle)
    }

    // ─── Login Flow ────────────────────────────────────────────────────────────

    @Test
    fun `onLoginClicked correcto con profileComplete=true emite NavigateToMain`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns Result.Success(fakeAuthToken(profileComplete = true))
        viewModel.onEmailChanged("test@aifit.com")
        viewModel.onPasswordChanged("password123")

        viewModel.events.test {
            viewModel.onLoginClicked()
            val event = awaitItem()
            assertTrue(event is AuthUiEvent.NavigateToMain)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onLoginClicked correcto con profileComplete=false emite NavigateToCreateProfile`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns Result.Success(fakeAuthToken(profileComplete = false))
        viewModel.onEmailChanged("test@aifit.com")
        viewModel.onPasswordChanged("password123")

        viewModel.events.test {
            viewModel.onLoginClicked()
            val event = awaitItem()
            assertTrue(event is AuthUiEvent.NavigateToCreateProfile)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onLoginClicked correcto setea uiState a Success`() = runTest {
        val token = fakeAuthToken()
        coEvery { loginUseCase(any(), any()) } returns Result.Success(token)
        viewModel.onEmailChanged("test@aifit.com")
        viewModel.onPasswordChanged("password123")

        viewModel.uiState.test {
            skipItems(1) // Idle
            viewModel.onLoginClicked()
            // Loading + Success (con UnconfinedTestDispatcher ambos se emiten)
            val first = awaitItem()
            when {
                first is AuthUiState.Loading -> {
                    val second = awaitItem()
                    assertTrue(second is AuthUiState.Success)
                    assertEquals(token, (second as AuthUiState.Success).token)
                }
                first is AuthUiState.Success -> {
                    assertEquals(token, (first as AuthUiState.Success).token)
                }
                else -> error("Estado inesperado: $first")
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onLoginClicked fallido emite ShowSnackbar y vuelve a Idle`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns
            Result.Error(AppException.NetworkException)
        viewModel.onEmailChanged("test@aifit.com")
        viewModel.onPasswordChanged("password123")

        viewModel.events.test {
            viewModel.onLoginClicked()
            val event = awaitItem()
            assertTrue(event is AuthUiEvent.ShowSnackbar)
            cancelAndIgnoreRemainingEvents()
        }
        assertTrue(viewModel.uiState.value is AuthUiState.Idle)
    }

    // ─── Register Flow ─────────────────────────────────────────────────────────

    @Test
    fun `onRegisterClicked correcto emite NavigateToCreateProfile`() = runTest {
        coEvery { registerUseCase(any(), any(), any()) } returns
            Result.Success(fakeAuthToken(profileComplete = false))
        viewModel.onEmailChanged("test@aifit.com")
        viewModel.onPasswordChanged("password123")
        viewModel.onNameChanged("Juan Test")

        viewModel.events.test {
            viewModel.onRegisterClicked()
            val event = awaitItem()
            assertTrue(event is AuthUiEvent.NavigateToCreateProfile)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onRegisterClicked fallido emite ShowSnackbar`() = runTest {
        coEvery { registerUseCase(any(), any(), any()) } returns
            Result.Error(AppException.ConflictException)
        viewModel.onEmailChanged("test@aifit.com")
        viewModel.onPasswordChanged("password123")
        viewModel.onNameChanged("Juan Test")

        viewModel.events.test {
            viewModel.onRegisterClicked()
            val event = awaitItem()
            assertTrue(event is AuthUiEvent.ShowSnackbar)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── Google Login Flow ─────────────────────────────────────────────────────

    @Test
    fun `onGoogleLoginResult correcto con profileComplete=true emite NavigateToMain`() = runTest {
        coEvery { googleLoginUseCase(any()) } returns Result.Success(fakeAuthToken(profileComplete = true))

        viewModel.events.test {
            viewModel.onGoogleLoginResult("google-token")
            val event = awaitItem()
            assertTrue(event is AuthUiEvent.NavigateToMain)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onGoogleLoginResult fallido emite ShowSnackbar y vuelve a Idle`() = runTest {
        coEvery { googleLoginUseCase(any()) } returns
            Result.Error(AppException.UnauthorizedException)

        viewModel.events.test {
            viewModel.onGoogleLoginResult("bad-token")
            val event = awaitItem()
            assertTrue(event is AuthUiEvent.ShowSnackbar)
            cancelAndIgnoreRemainingEvents()
        }
        assertTrue(viewModel.uiState.value is AuthUiState.Idle)
    }

    // ─── Navegacion ────────────────────────────────────────────────────────────

    @Test
    fun `onNavigateToRegister emite NavigateToRegister`() = runTest {
        viewModel.events.test {
            viewModel.onNavigateToRegister()
            assertTrue(awaitItem() is AuthUiEvent.NavigateToRegister)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onNavigateBack emite NavigateBack`() = runTest {
        viewModel.events.test {
            viewModel.onNavigateBack()
            assertTrue(awaitItem() is AuthUiEvent.NavigateBack)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
