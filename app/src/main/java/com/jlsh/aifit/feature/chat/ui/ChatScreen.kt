package com.jlsh.aifit.feature.chat.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.chat.ChatBubble
import com.jlsh.aifit.core.ui.components.chat.ChatInputBar
import com.jlsh.aifit.core.ui.components.chat.TypingIndicator
import com.jlsh.aifit.core.ui.components.feedback.ConfirmationDialog
import com.jlsh.aifit.core.ui.components.feedback.LoadingScreen
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.components.layout.LocalBottomBarVisibility
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.chat.domain.model.ChatMessage
import com.jlsh.aifit.feature.chat.domain.model.ChatMessageRole
import com.jlsh.aifit.feature.chat.ui.state.ChatState
import com.jlsh.aifit.feature.chat.ui.state.ChatUiEvent

@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val chatState by viewModel.chatState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showArchiveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ChatUiEvent.NavigateBack -> onNavigateBack()
                is ChatUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    // Hide bottom nav
    CompositionLocalProvider(LocalBottomBarVisibility provides false) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                AiFitTopBar(
                    title = chatState.sessionTitle.ifBlank { "Chat" },
                    onBack = onNavigateBack,
                    background = MaterialTheme.colorScheme.background,
                    actions = {
                        IconButton(onClick = { showArchiveDialog = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Archive,
                                contentDescription = "Archivar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            when {
                chatState.isLoading -> LoadingScreen()
                chatState.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = chatState.error ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                else -> {
                    ChatContent(
                        paddingValues = paddingValues,
                        state = chatState,
                        onInputChanged = viewModel::onInputChanged,
                        onSend = viewModel::onSendMessage,
                    )
                }
            }
        }
    }

    if (showArchiveDialog) {
        ConfirmationDialog(
            title = "Archivar sesión",
            message = "¿Seguro que quieres archivar esta conversación?",
            confirmText = "Archivar",
            onConfirm = {
                showArchiveDialog = false
                viewModel.onArchiveCurrentSession()
            },
            onDismiss = { showArchiveDialog = false },
        )
    }
}

@Composable
private fun ChatContent(
    paddingValues: PaddingValues,
    state: ChatState,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
) {
    val listState = rememberLazyListState()

    // Auto-scroll when new message arrives
    LaunchedEffect(state.messages.size, state.isWaitingResponse) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        // Messages
        LazyColumn(
            state = listState,
            reverseLayout = true,
            contentPadding = PaddingValues(
                start = AiFitSpacing.md,
                end = AiFitSpacing.md,
                top = AiFitSpacing.sm,
                bottom = AiFitSpacing.sm,
            ),
            modifier = Modifier.weight(1f),
        ) {
            // Typing indicator (appears first visually = bottom)
            if (state.isWaitingResponse) {
                item(key = "typing") {
                    TypingIndicator(
                        modifier = Modifier.padding(vertical = AiFitSpacing.sm),
                    )
                }
            }

            // Messages in reverse order (newest first for reverseLayout)
            items(
                items = state.messages.reversed(),
                key = { it.id },
            ) { message ->
                ChatBubble(
                    content = message.content,
                    isUser = message.role == ChatMessageRole.USER,
                    timestamp = formatTimestamp(message.createdAt),
                    isMarkdown = message.role == ChatMessageRole.ASSISTANT,
                    modifier = Modifier.padding(vertical = AiFitSpacing.xs),
                )
            }
        }

        // Input bar (sticky bottom)
        ChatInputBar(
            value = state.inputText,
            onValueChange = onInputChanged,
            onSend = onSend,
            isLoading = state.isWaitingResponse,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun formatTimestamp(isoString: String): String {
    return runCatching {
        val instant = java.time.Instant.parse(isoString)
        val localTime = instant.atZone(java.time.ZoneId.systemDefault()).toLocalTime()
        "%02d:%02d".format(localTime.hour, localTime.minute)
    }.getOrDefault("")
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "ChatScreen Dark",
)
@Composable
private fun ChatScreenPreview() {
    AIFitTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            ChatContent(
                paddingValues = PaddingValues(),
                state = ChatState(
                    messages = listOf(
                        ChatMessage(
                            id = "1",
                            role = ChatMessageRole.ASSISTANT,
                            content = "¡Hola! Soy tu **AI Coach**. ¿En qué puedo ayudarte hoy?\n\n- Crear un plan de entrenamiento\n- Ajustar tu dieta\n- Resolver dudas",
                            createdAt = "2025-03-10T10:00:00Z",
                        ),
                        ChatMessage(
                            id = "2",
                            role = ChatMessageRole.USER,
                            content = "Necesito un plan para ganar masa muscular",
                            createdAt = "2025-03-10T10:01:00Z",
                        ),
                        ChatMessage(
                            id = "3",
                            role = ChatMessageRole.ASSISTANT,
                            content = "Perfecto. Basándome en tu perfil, te recomiendo un programa de **hipertrofia** de 4 días por semana.\n\n```\nLunes: Pecho + Tríceps\nMartes: Espalda + Bíceps\nJueves: Piernas\nViernes: Hombros + Core\n```",
                            createdAt = "2025-03-10T10:02:00Z",
                        ),
                    ),
                    sessionTitle = "Plan de hipertrofia",
                    isLoading = false,
                ),
                onInputChanged = {},
                onSend = {},
            )
        }
    }
}



