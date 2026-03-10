package com.jlsh.aifit.core.ui.components.layout

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jlsh.aifit.core.ui.components.feedback.ErrorScreen
import com.jlsh.aifit.core.ui.components.feedback.LoadingScreen
import com.jlsh.aifit.core.ui.theme.AIFitTheme

interface UiStateHost {
    interface Loading
    interface Error {
        val message: String
    }
    interface Success
}

@Composable
fun <S : UiStateHost.Success> ScreenScaffold(
    uiState: Any,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    onRetry: () -> Unit = {},
    content: @Composable (PaddingValues, S) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        when (uiState) {
            is UiStateHost.Loading -> LoadingScreen()
            is UiStateHost.Error -> ErrorScreen(
                message = uiState.message,
                onRetry = onRetry,
            )
            is UiStateHost.Success -> {
                @Suppress("UNCHECKED_CAST")
                content(paddingValues, uiState as S)
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark"
)
@Composable
private fun ScreenScaffoldPreview() {
    AIFitTheme(darkTheme = true) {
        LoadingScreen()
    }
}
