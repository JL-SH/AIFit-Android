package com.jlsh.aifit.feature.auth.ui

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.jlsh.aifit.BuildConfig
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.buttons.GoogleSignInButton
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.display.AiFitLogoSplit
import com.jlsh.aifit.core.ui.components.inputs.AiFitPasswordField
import com.jlsh.aifit.core.ui.components.inputs.AiFitTextField
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.feature.auth.ui.state.AuthUiEvent
import com.jlsh.aifit.feature.auth.ui.state.AuthUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToCreateProfile: () -> Unit,
    sessionExpiredMessage: String? = null,
    onSessionExpiredMessageShown: () -> Unit = {},
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

    val credentialManager = remember { CredentialManager.create(context) }
    val signInWithGoogleRequest = remember {
        GetCredentialRequest.Builder()
            .addCredentialOption(
                GetSignInWithGoogleOption
                    .Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                    .build()
            )
            .build()
    }
    var isGoogleSignInInProgress by remember { mutableStateOf(false) }

    LaunchedEffect(sessionExpiredMessage) {
        if (!sessionExpiredMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(sessionExpiredMessage)
            onSessionExpiredMessageShown()
        }
    }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                AiFitLogoSplit()

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.auth_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(32.dp))

                AiFitTextField(
                    value = email,
                    onValueChange = viewModel::onEmailChanged,
                    label = stringResource(R.string.auth_email_label),
                    error = emailError,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                AiFitPasswordField(
                    value = password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = stringResource(R.string.auth_password_label),
                    error = passwordError,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = stringResource(R.string.auth_login_button),
                    onClick = viewModel::onLoginClicked,
                    isLoading = uiState is AuthUiState.Loading,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    Text(
                        text = " ${stringResource(R.string.common_or)} ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                GoogleSignInButton(
                    text = stringResource(R.string.auth_google_button),
                    onClick = {
                        if (isGoogleSignInInProgress) return@GoogleSignInButton
                        scope.launch {
                            isGoogleSignInInProgress = true
                            Log.d("AIFIT", "Google Sign-In: iniciando flujo — WebClientId=${BuildConfig.GOOGLE_WEB_CLIENT_ID.take(20)}…")
                            try {
                                Log.d("AIFIT", "Google Sign-In: lanzando GetSignInWithGoogleOption")
                                val result = credentialManager.getCredential(context, signInWithGoogleRequest)
                                val credential = result.credential
                                Log.d("AIFIT", "Google Sign-In: credencial recibida, tipo=${credential.type}")
                                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                    val googleIdTokenCredential =
                                        GoogleIdTokenCredential.createFrom(credential.data)
                                    val idToken = googleIdTokenCredential.idToken
                                    Log.d("AIFIT", "Google Sign-In: idToken obtenido (${idToken.length} chars)")
                                    viewModel.onGoogleLoginResult(idToken)
                                } else {
                                    Log.e("AIFIT", "Google Sign-In: tipo de credencial inesperado: ${credential.type}")
                                    snackbarHostState.showSnackbar(
                                        context.getString(R.string.auth_google_error)
                                    )
                                }
                            } catch (_: GetCredentialCancellationException) {
                                Log.d("AIFIT", "Google Sign-In: usuario canceló el flujo")
                            } catch (e: GoogleIdTokenParsingException) {
                                Log.e("AIFIT", "Google Sign-In: error al parsear el token de Google", e)
                                snackbarHostState.showSnackbar(
                                    context.getString(R.string.auth_google_error)
                                )
                            } catch (e: GetCredentialException) {
                                Log.e("AIFIT", "Google Sign-In: error de CredentialManager — type=${e.type}, msg=${e.message}", e)
                                snackbarHostState.showSnackbar(
                                    e.localizedMessage ?: context.getString(R.string.auth_google_error)
                                )
                            } catch (e: CancellationException) {
                                throw e  // Cancelación de ciclo de vida, no es un error
                            } catch (e: Exception) {
                                Log.e("AIFIT", "Google Sign-In: error inesperado — ${e.javaClass.simpleName}: ${e.message}", e)
                                snackbarHostState.showSnackbar(
                                    e.localizedMessage ?: context.getString(R.string.auth_google_error)
                                )
                            } finally {
                                isGoogleSignInInProgress = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.auth_no_account),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.auth_create_one),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.clickable { onNavigateToRegister() },
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "LoginScreen Dark",
)
@Composable
private fun LoginScreenPreview() {
    AIFitTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                AiFitLogoSplit()

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.login_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(32.dp))

                AiFitTextField(
                    value = "usuario@ejemplo.com",
                    onValueChange = {},
                    label = stringResource(R.string.login_email_label),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                AiFitPasswordField(
                    value = "••••••••",
                    onValueChange = {},
                    label = stringResource(R.string.login_password_label),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = stringResource(R.string.login_sign_in_btn),
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    Text(
                        text = " o ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                GoogleSignInButton(
                    text = stringResource(R.string.login_google_btn),
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "¿No tienes cuenta?  ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.login_create_account),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
