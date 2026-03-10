package com.jlsh.aifit.feature.auth.ui

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.display.AiFitLogoSplit
import com.jlsh.aifit.core.ui.components.inputs.AiFitPasswordField
import com.jlsh.aifit.core.ui.components.inputs.AiFitTextField
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.feature.auth.ui.state.AuthUiEvent
import com.jlsh.aifit.feature.auth.ui.state.AuthUiState

@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateProfile: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val email by viewModel.email.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val name by viewModel.name.collectAsStateWithLifecycle()
    val emailError by viewModel.emailError.collectAsStateWithLifecycle()
    val passwordError by viewModel.passwordError.collectAsStateWithLifecycle()
    val nameError by viewModel.nameError.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthUiEvent.NavigateToMain -> onNavigateToMain()
                is AuthUiEvent.NavigateToCreateProfile -> onNavigateToCreateProfile()
                is AuthUiEvent.NavigateToRegister -> { /* already here */ }
                is AuthUiEvent.NavigateBack -> onNavigateBack()
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AiFitLogoSplit()

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Crea tu cuenta",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(32.dp))

                AiFitTextField(
                    value = name,
                    onValueChange = viewModel::onNameChanged,
                    label = "Nombre completo",
                    error = nameError,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                AiFitTextField(
                    value = email,
                    onValueChange = viewModel::onEmailChanged,
                    label = "Email",
                    error = emailError,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                AiFitPasswordField(
                    value = password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = "Contraseña",
                    error = passwordError,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = "CREAR CUENTA",
                    onClick = viewModel::onRegisterClicked,
                    isLoading = uiState is AuthUiState.Loading,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "¿Ya tienes cuenta?  ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Inicia sesión",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.clickable { viewModel.onNavigateBack() },
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
    name = "RegisterScreen Dark",
)
@Composable
private fun RegisterScreenPreview() {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "AIFit",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Crea tu cuenta",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(32.dp))

                AiFitTextField(
                    value = "Carlos García",
                    onValueChange = {},
                    label = "Nombre completo",
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                AiFitTextField(
                    value = "usuario@ejemplo.com",
                    onValueChange = {},
                    label = "Email",
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                AiFitPasswordField(
                    value = "••••••••",
                    onValueChange = {},
                    label = "Contraseña",
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = "CREAR CUENTA",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "¿Ya tienes cuenta?  ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Inicia sesión",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

