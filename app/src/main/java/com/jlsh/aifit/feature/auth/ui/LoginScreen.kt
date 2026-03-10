package com.jlsh.aifit.feature.auth.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.jlsh.aifit.BuildConfig
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.buttons.SecondaryButton
import com.jlsh.aifit.core.ui.components.inputs.AiFitPasswordField
import com.jlsh.aifit.core.ui.components.inputs.AiFitTextField
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.auth.ui.state.AuthUiEvent
import com.jlsh.aifit.feature.auth.ui.state.AuthUiState
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToCreateProfile: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val email by viewModel.email.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val emailError by viewModel.emailError.collectAsStateWithLifecycle()
    val passwordError by viewModel.passwordError.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthUiEvent.NavigateToMain -> onNavigateToMain()
                is AuthUiEvent.NavigateToCreateProfile -> onNavigateToCreateProfile()
                is AuthUiEvent.NavigateToRegister -> onNavigateToRegister()
                is AuthUiEvent.NavigateBack -> { /* no back from login */ }
                is AuthUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = AiFitSpacing.md)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(AiFitSpacing.xxl))

            Text(
                text = "AIFit",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primaryContainer,
            )

            Spacer(modifier = Modifier.height(AiFitSpacing.xxl))

            AiFitTextField(
                value = email,
                onValueChange = viewModel::onEmailChanged,
                label = "Email",
                error = emailError,
            )

            Spacer(modifier = Modifier.height(AiFitSpacing.md))

            AiFitPasswordField(
                value = password,
                onValueChange = viewModel::onPasswordChanged,
                label = "Contraseña",
                error = passwordError,
            )

            Spacer(modifier = Modifier.height(AiFitSpacing.lg))

            PrimaryButton(
                text = "Iniciar sesión",
                onClick = viewModel::onLoginClicked,
                isLoading = uiState is AuthUiState.Loading,
            )

            Spacer(modifier = Modifier.height(AiFitSpacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                Text(
                    text = "o",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }

            Spacer(modifier = Modifier.height(AiFitSpacing.lg))

            SecondaryButton(
                text = "Continuar con Google",
                onClick = {
                    scope.launch {
                        try {
                            val credentialManager = CredentialManager.create(context)
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                                .build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()
                            val result = credentialManager.getCredential(context, request)
                            val credential = result.credential
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            viewModel.onGoogleLoginResult(googleIdTokenCredential.idToken)
                        } catch (_: GetCredentialCancellationException) {
                            // User cancelled — do nothing
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar(
                                e.localizedMessage ?: "Error al iniciar sesión con Google"
                            )
                        }
                    }
                },
                isLoading = false,
            )

            Spacer(modifier = Modifier.height(AiFitSpacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "¿No tienes cuenta? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Crear una",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.clickable { viewModel.onNavigateToRegister() },
                )
            }

            Spacer(modifier = Modifier.height(AiFitSpacing.xxl))
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark"
)
@Composable
private fun LoginScreenPreview() {
    AIFitTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AiFitSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(AiFitSpacing.xxl))
            Text(
                text = "AIFit",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primaryContainer,
                textAlign = TextAlign.Center,
            )
        }
    }
}

