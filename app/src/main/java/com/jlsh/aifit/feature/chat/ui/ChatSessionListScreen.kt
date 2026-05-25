package com.jlsh.aifit.feature.chat.ui

import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jlsh.aifit.R
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.display.PlanStatusBadge
import com.jlsh.aifit.core.ui.components.feedback.ConfirmationDialog
import com.jlsh.aifit.core.ui.components.feedback.EmptyStateKind
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

/**
 * Screen listing conversations with the AI ​​Coach.
 *
 * Shows sessions with swipe to archive/delete, FAB for new chat, dialogs
 * confirmation of deletion and renaming. React to [ChatUiEvent] for navigation and snackbars.
 *
 * @param onNavigateToChat Opens an existing session by its identifier.
 * @param onNavigateToNewChat Opens an empty chat without creating a session in the backend until the first send.
 * @param viewModel Chat viewModel injected by Hilt.
 */
@Composable
fun ChatSessionListScreen(
    onNavigateToChat: (sessionId: String) -> Unit,
    onNavigateToNewChat: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val listState by viewModel.listState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var deleteDialogSessionId by remember { mutableStateOf<String?>(null) }
    var renameDialogSession by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(Unit) { viewModel.loadSessions() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ChatUiEvent.SessionCreated -> onNavigateToChat(event.sessionId)
                is ChatUiEvent.NavigateToChat -> onNavigateToChat(event.sessionId)
                is ChatUiEvent.NavigateToNewChat -> onNavigateToNewChat()
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
                title = stringResource(R.string.chat_title),
                background = MaterialTheme.colorScheme.secondaryContainer,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::onNewSession,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(imageVector = PhosphorIcons.Regular.Plus, contentDescription = stringResource(R.string.chat_new_cd))
            }
        },
        onRetry = viewModel::loadSessions,
    ) { paddingValues, successState ->
        if (successState.sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                EmptyStateView(
                    icon = PhosphorIcons.Regular.Robot,
                    kind = EmptyStateKind.ChatSessions,
                    title = stringResource(R.string.chat_empty_title),
                    subtitle = stringResource(R.string.chat_empty_subtitle),
                )
            }
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
                            onRename = { renameDialogSession = session.id to session.title },
                            onDelete = { deleteDialogSessionId = session.id },
                        )
                    }
                }
            }
        }
    }

    deleteDialogSessionId?.let { id ->
        ConfirmationDialog(
            title = stringResource(R.string.chat_delete_dialog_title),
            message = stringResource(R.string.chat_delete_dialog_message),
            confirmText = stringResource(R.string.chat_delete_confirm),
            dismissText = stringResource(R.string.common_cancel),
            onConfirm = {
                viewModel.onDeleteSession(id)
                deleteDialogSessionId = null
            },
            onDismiss = { deleteDialogSessionId = null },
        )
    }

    renameDialogSession?.let { (id, currentTitle) ->
        RenameSessionDialog(
            currentTitle = currentTitle,
            onConfirm = { newTitle ->
                viewModel.onRenameSession(id, newTitle)
                renameDialogSession = null
            },
            onDismiss = { renameDialogSession = null },
        )
    }
}

@Composable
private fun SessionRow(
    session: ChatSession,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = AiFitSpacing.sm, end = 0.dp, top = AiFitSpacing.xs, bottom = AiFitSpacing.xs),
    ) {
        // ── Row 1: title (truncated) + archived badge ───────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = session.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (session.status == ChatSessionStatus.ARCHIVED) {
                PlanStatusBadge(status = "ARCHIVED")
            }
        }

        // ── Row 2: messages | date | buttons ────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.chat_message_count, session.messageCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = session.updatedAt.take(10),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            IconButton(onClick = onRename, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = PhosphorIcons.Regular.PencilSimple,
                    contentDescription = stringResource(R.string.chat_rename_cd),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Trash,
                    contentDescription = stringResource(R.string.chat_delete_cd),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun RenameSessionDialog(
    currentTitle: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(currentTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.chat_rename_dialog_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.chat_rename_name_label)) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onConfirm(text) }) {
                Text(stringResource(R.string.chat_rename_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        },
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "SessionRow Dark")
@Composable
private fun SessionRowPreview() {
    AIFitTheme(darkTheme = true) {
        SessionRow(
            session = ChatSession(
                id = "1",
                title = "Plan de hipertrofia para principiantes",
                status = ChatSessionStatus.ACTIVE,
                messages = emptyList(),
                createdAt = "2025-03-10T10:00:00Z",
                updatedAt = "2025-03-10T12:30:00Z",
                messageCount = 8,
            ),
            onClick = {},
            onRename = {},
            onDelete = {},
        )
    }
}
