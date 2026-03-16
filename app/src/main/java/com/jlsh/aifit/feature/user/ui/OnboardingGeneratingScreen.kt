package com.jlsh.aifit.feature.user.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import kotlinx.coroutines.delay

@Composable
fun OnboardingGeneratingScreen(
    onSuccess: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showStep1 by remember { mutableStateOf(false) }
    var showStep2 by remember { mutableStateOf(false) }
    var showStep3 by remember { mutableStateOf(false) }
    var animationStartTime by remember { mutableStateOf(0L) }

    // Launch generation and animation in parallel
    LaunchedEffect(Unit) {
        animationStartTime = System.currentTimeMillis()
        if (viewModel.state.value is OnboardingState.Idle) {
            viewModel.generatePlan()
        }
        showStep1 = true
        delay(1500L)
        showStep2 = true
        delay(1500L)
        showStep3 = true
    }

    // Observe state for success/error
    LaunchedEffect(state) {
        when (val currentState = state) {
            is OnboardingState.Ready -> {
                val elapsed = System.currentTimeMillis() - animationStartTime
                val remaining = 4000L - elapsed
                if (remaining > 0) delay(remaining)
                onSuccess()
            }
            is OnboardingState.Error -> {
                val result = snackbarHostState.showSnackbar(
                    message = currentState.message,
                    actionLabel = "Reintentar",
                )
                if (result == SnackbarResult.ActionPerformed) {
                    animationStartTime = System.currentTimeMillis()
                    showStep1 = true
                    showStep2 = false
                    showStep3 = false
                    viewModel.generatePlan()
                }
            }
            else -> Unit
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
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = AiFitSpacing.xl),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AnimatedVisibility(
                    visible = showStep1,
                    enter = fadeIn(),
                ) {
                    Text(
                        text = "✦ Analizando tu perfil...",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(Modifier.height(AiFitSpacing.md))

                AnimatedVisibility(
                    visible = showStep2,
                    enter = fadeIn(),
                ) {
                    Text(
                        text = "✦ Generando tu plan de entrenamiento...",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(Modifier.height(AiFitSpacing.md))

                AnimatedVisibility(
                    visible = showStep3,
                    enter = fadeIn(),
                ) {
                    Text(
                        text = "✦ Preparando tu plan de nutrición...",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

