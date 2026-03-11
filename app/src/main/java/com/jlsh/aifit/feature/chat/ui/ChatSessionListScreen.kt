package com.jlsh.aifit.feature.chat.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.display.PlanStatusBadge
import com.jlsh.aifit.core.ui.components.feedback.ConfirmationDialog
import com.jlsh.aifit.core.ui.components.feedback.EmptyStateView
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.components.layout.ScreenScaffold
import com.jlsh.aifit.core.ui.components.list.SwipeableListItem
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.chat.domain.model.ChatSession
import com.jlsh.aifit.feature.chat.domain.model.ChatSessionStatus
import com.jlsh.aifit.feature.chat.ui.state.ChatListUiState
import com.jlsh.aifit.feature.chat.ui.state.ChatUiEvent

@Composable
fun ChatSessionListScreen(
    onNavigateToChat: (sessionId: String) -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val listState by viewModel.listState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var deleteDialogSessionId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadSessions()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ChatUiEvent.SessionCreated -> onNavigateToChat(event.sessionId)
                is ChatUiEvent.NavigateToChat -> onNavigateToChat(event.sessionId)
                is ChatUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is ChatUiEvent.NavigateBack -> { /* not applicable here */ }
            }
        }
    }

    ScreenScaffold<ChatListUiState.Success>(
        uiState = listState,
        snackbarHostState = snackbarHostState,
        topBar = {
            AiFitTopBar(
                title = "AI Coach",
                background = MaterialTheme.colorScheme.secondaryContainer,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::onNewSession,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Nuevo chat",
                )
            }
        },
        onRetry = viewModel::loadSessions,
    ) { paddingValues, successState ->
        if (successState.sessions.isEmpty()) {
            EmptyStateView(
                icon = Icons.Rounded.SmartToy,
                title = "Sin conversaciones",
                subtitle = "Inicia una conversación con tu AI Coach",
                modifier = Modifier.padding(paddingValues),
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = AiFitSpacing.md,
                    end = AiFitSpacing.md,
                    top = AiFitSpacing.sm,
                    bottom = 88.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
                modifier = Modifier.padding(paddingValues),
            ) {
                items(successState.sessions, key = { it.id }) { session ->
                    SwipeableListItem(
                        onDelete = { deleteDialogSessionId = session.id },
                        onArchive = { viewModel.onArchiveSession(session.id) },
                    ) {
                        SessionRow(
                            session = session,
                            onClick = { onNavigateToChat(session.id) },
                        )
                    }
                }
            }
        }
    }

    deleteDialogSessionId?.let { id ->
        ConfirmationDialog(
            title = "Eliminar sesión",
            message = "¿Seguro que quieres eliminar esta conversación?",
            confirmText = "Eliminar",
            onConfirm = {
                viewModel.onDeleteSession(id)
                deleteDialogSessionId = null
            },
            onDismiss = { deleteDialogSessionId = null },
        )
    }
}

@Composable
private fun SessionRow(
    session: ChatSession,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = AiFitSpacing.sm, horizontal = AiFitSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = session.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (session.status == ChatSessionStatus.ARCHIVED) {
                PlanStatusBadge(status = "ARCHIVED")
            }
        }
        Spacer(modifier = Modifier.height(AiFitSpacing.xs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "${session.messageCount} mensajes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = session.updatedAt.take(10),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "ChatSessionList Dark",
)
@Composable
private fun ChatSessionListPreview() {
    AIFitTheme(darkTheme = true) {
        SessionRow(
            session = ChatSession(
                id = "1",
                title = "Plan de entrenamiento",
                status = ChatSessionStatus.ACTIVE,
                messages = emptyList(),
                createdAt = "2025-03-10T10:00:00Z",
                updatedAt = "2025-03-10T12:30:00Z",
                messageCount = 8,
            ),
            onClick = {},
        )
    }
}



