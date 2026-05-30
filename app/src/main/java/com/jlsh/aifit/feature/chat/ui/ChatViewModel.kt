package com.jlsh.aifit.feature.chat.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.chat.domain.model.ChatMessage
import com.jlsh.aifit.feature.chat.domain.model.ChatMessageRole
import com.jlsh.aifit.feature.chat.domain.usecase.ArchiveChatSessionUseCase
import com.jlsh.aifit.feature.chat.domain.usecase.DeleteChatSessionUseCase
import com.jlsh.aifit.feature.chat.domain.usecase.GenerateChatSessionTitleUseCase
import com.jlsh.aifit.feature.chat.domain.usecase.GetChatSessionUseCase
import com.jlsh.aifit.feature.chat.domain.usecase.GetChatSessionsUseCase
import com.jlsh.aifit.feature.chat.domain.usecase.RenameChatSessionUseCase
import com.jlsh.aifit.feature.chat.domain.usecase.SendChatMessageUseCase
import com.jlsh.aifit.feature.chat.domain.usecase.StartChatSessionUseCase
import com.jlsh.aifit.feature.chat.ui.state.ChatListUiState
import com.jlsh.aifit.feature.chat.ui.state.ChatState
import com.jlsh.aifit.feature.chat.ui.state.ChatUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel shared by the list of sessions and the active conversation with the AI ​​Coach.
 *
 * **ListUiState** ([listState] — [ChatListUiState]):
 * - [ChatListUiState.Loading]: loading sessions.
 * - [ChatListUiState.Success]: Active sessions (excludes archived and optimistic deletes).
 * - [ChatListUiState.Error]: error message.
 *
 * **Chat UiState** ([chatState] — [ChatState]):
 * - Messages, input text, pending image, session title.
 * - [ChatState.isLoading]: Load existing session.
 * - [ChatState.isWaitingResponse]: waiting for response from the assistant.
 * - [ChatState.error]: Error loading session.
 *
 * **Emitted events** ([events] — [ChatUiEvent]):
 * - [ChatUiEvent.NavigateToChat]: Open existing session.
 * - [ChatUiEvent.NavigateToNewChat]: Start empty chat without creating session in backend.
 * - [ChatUiEvent.NavigateBack]: Return after archiving the current session.
 * - [ChatUiEvent.SessionCreated]: Session created (reserved for future flows).
 * - [ChatUiEvent.ShowSnackbar]: message to the user.
 *
 * @param savedStateHandle Reads `sessionId` from the path; `null` indicates new chat.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getChatSessionsUseCase: GetChatSessionsUseCase,
    private val getChatSessionUseCase: GetChatSessionUseCase,
    private val startChatSessionUseCase: StartChatSessionUseCase,
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val archiveChatSessionUseCase: ArchiveChatSessionUseCase,
    private val deleteChatSessionUseCase: DeleteChatSessionUseCase,
    private val renameChatSessionUseCase: RenameChatSessionUseCase,
    private val generateChatSessionTitleUseCase: GenerateChatSessionTitleUseCase,
) : ViewModel() {

    // ── Session List State ───────────────────────────────────────────────────
    private val _listState = MutableStateFlow<ChatListUiState>(ChatListUiState.Loading)

    /** Status of the conversation list.*/
    val listState: StateFlow<ChatListUiState> = _listState.asStateFlow()

    // IDs pending network deletion (filtered out in loadSessions)
    private val pendingDeleteIds = mutableSetOf<String>()

    // ── Active Chat State ────────────────────────────────────────────────────
    private val _chatState = MutableStateFlow(ChatState())

    /** Status of the active conversation (messages, entry, upload).*/
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    // ── Events ───────────────────────────────────────────────────────────────
    private val _events = Channel<ChatUiEvent>(Channel.BUFFERED)

    /** Navigation flow and snackbars; consume once per screen.*/
    val events = _events.receiveAsFlow()

    // Can be null when the user navigates to "new chat" without having sent anything yet.
    // The real id is assigned in the first sending.
    private val savedSessionId: String? = savedStateHandle.get<String>("sessionId")
    private var effectiveSessionId: String? = savedSessionId

    init {
        if (savedSessionId != null) {
            loadSession(savedSessionId)
        } else {
            // New chat: show empty screen ready to write
            _chatState.value = ChatState(isLoading = false)
            loadSessions()
        }
    }

    // ── Session List ─────────────────────────────────────────────────────────

    /** Reload the session list from the repository (cache + network).*/
    fun loadSessions() {
        viewModelScope.launch {
            getChatSessionsUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _listState.value = ChatListUiState.Success(
                            sessions = result.data.filter {
                                it.status != com.jlsh.aifit.feature.chat.domain.model.ChatSessionStatus.ARCHIVED
                                    && it.id !in pendingDeleteIds
                            },
                        )
                    }
                    is Result.Error -> {
                        _listState.value = ChatListUiState.Error(result.exception.userMessage())
                    }
                    is Result.Loading -> {
                        _listState.value = ChatListUiState.Loading
                    }
                }
            }
        }
    }

    /** Issues [ChatUiEvent.NavigateToNewChat] without creating a session in the backend.*/
    fun onNewSession() {
        // Navigate to new chat WITHOUT calling the backend yet
        viewModelScope.launch {
            _events.send(ChatUiEvent.NavigateToNewChat)
        }
    }

    /**
     * Delete a session with optimistic delete from the list.
     *
     * @param id Identifier of the session to delete.
     */
    fun onDeleteSession(id: String) {
        // Optimistic delete: remove immediately from local state
        val previousState = _listState.value
        pendingDeleteIds.add(id)
        if (previousState is ChatListUiState.Success) {
            _listState.value = previousState.copy(
                sessions = previousState.sessions.filter { it.id != id }
            )
        }
        viewModelScope.launch {
            when (val result = deleteChatSessionUseCase(id)) {
                is Result.Success -> {
                    pendingDeleteIds.remove(id)
                    _events.send(ChatUiEvent.ShowSnackbar("Sesión eliminada"))
                    loadSessions()
                }
                is Result.Error -> {
                    // Rollback on error
                    pendingDeleteIds.remove(id)
                    _listState.value = previousState
                    _events.send(ChatUiEvent.ShowSnackbar(result.exception.userMessage()))
                }
                else -> Unit
            }
        }
    }

    /**
     * Archive a session and reload the list.
     *
     * @param id Identifier of the session to archive.
     */
    fun onArchiveSession(id: String) {
        viewModelScope.launch {
            when (val result = archiveChatSessionUseCase(id)) {
                is Result.Success -> {
                    loadSessions()
                    _events.send(ChatUiEvent.ShowSnackbar("Sesión archivada"))
                }
                is Result.Error -> {
                    _events.send(ChatUiEvent.ShowSnackbar(result.exception.userMessage()))
                }
                else -> Unit
            }
        }
    }

    /**
     * Rename a session and reload the list.
     *
     * @param id Session identifier.
     * @param newTitle New title (whitespace is trimmed).
     */
    fun onRenameSession(id: String, newTitle: String) {
        viewModelScope.launch {
            when (val result = renameChatSessionUseCase(id, newTitle.trim())) {
                is Result.Success -> {
                    loadSessions()
                    _events.send(ChatUiEvent.ShowSnackbar("Conversación renombrada"))
                }
                is Result.Error -> {
                    _events.send(ChatUiEvent.ShowSnackbar(result.exception.userMessage()))
                }
                else -> Unit
            }
        }
    }

    // ── Active Chat ──────────────────────────────────────────────────────────

    /**
     * Loads messages and metadata from an existing session.
     *
     * @param id Identifier of the chat session.
     */
    /** Retry loading the active session after a network or server error.*/
    fun retrySessionLoad() {
        effectiveSessionId?.let { loadSession(it) }
    }

    fun loadSession(id: String) {
        viewModelScope.launch {
            _chatState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    messages = emptyList(),
                    sessionTitle = "",
                )
            }
            var loadFailed = false
            getChatSessionUseCase(id).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _chatState.update {
                            it.copy(
                                messages = result.data.messages,
                                sessionTitle = result.data.title,
                                error = null,
                            )
                        }
                    }
                    is Result.Error -> {
                        loadFailed = true
                        _chatState.update {
                            it.copy(isLoading = false, error = result.exception.userMessage())
                        }
                    }
                    is Result.Loading -> Unit
                }
            }
            if (!loadFailed) {
                _chatState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Updates the input field text.
     *
     * @param text Current content of the input.
     */
    fun onInputChanged(text: String) {
        _chatState.update { it.copy(inputText = text) }
    }

    /**
     * Stores a compressed gallery image as an attachment pending delivery.
     *
     * @param rawBytes Original bytes of the selected image.
     */
    fun onImageSelected(rawBytes: ByteArray) {
        viewModelScope.launch(Dispatchers.Default) {
            val compressed = compressImageBytes(rawBytes)
            safeLogDebug("onImageSelected: raw=${rawBytes.size / 1024}KB → compressed=${compressed.size / 1024}KB")
            _chatState.update { it.copy(pendingImageBytes = compressed) }
        }
    }

    /**
     * Decodes the bytes in a Bitmap, scales it to max. MAX_IMAGE_PX × MAX_IMAGE_PX
     * and re-encodes it as JPEG at 70% quality.
     * Typical result: 4 MB photo → ~80-120 KB → Base64 ~160 KB (workable in HTTP JSON).
     */
    private fun compressImageBytes(raw: ByteArray): ByteArray {
        val original = BitmapFactory.decodeByteArray(raw, 0, raw.size)
            ?: return raw  // fallback: use raw bytes when decode fails

        val maxPx = MAX_IMAGE_PX
        val (w, h) = original.width to original.height
        val scaled = if (w > maxPx || h > maxPx) {
            val ratio = maxPx.toFloat() / maxOf(w, h)
            Bitmap.createScaledBitmap(original, (w * ratio).toInt(), (h * ratio).toInt(), true)
        } else {
            original
        }

        val out = java.io.ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, out)
        if (scaled !== original) scaled.recycle()
        original.recycle()
        return out.toByteArray()
    }

    /** Descarta la imagen adjunta pendiente sin enviarla. */
    fun onClearPendingImage() {
        _chatState.update { it.copy(pendingImageBytes = null) }
    }

    /**
     * Send the current message (text and/or image). Create the session on the first submission if it doesn't already exist.
     * After the first exchange, request an AI-generated title or use a local fallback.
     */
    fun onSendMessage() {
        val content = _chatState.value.inputText.trim()
        val imageBytes = _chatState.value.pendingImageBytes
        if ((content.isBlank() && imageBytes == null) || content.length > 4000) {
            safeLogWarn("onSendMessage: contenido inválido (blank=${content.isBlank()}, hasImage=${imageBytes != null}, len=${content.length})")
            return
        }

        // Is it the first message of a new session (even without ID)?
        val isNewSession = effectiveSessionId == null
        val isFirstMessage = _chatState.value.messages.isEmpty()

        // Convert image to Base64 when present
        val imageBase64 = imageBytes?.let { Base64.encodeToString(it, Base64.NO_WRAP) }

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = ChatMessageRole.USER,
            content = content.ifBlank { "📷 Imagen adjunta" },
            createdAt = Instant.now().toString(),
            imageBase64 = imageBase64,
        )

        _chatState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                pendingImageBytes = null,
                isWaitingResponse = true,
            )
        }

        viewModelScope.launch {
            // If there is no session yet, create it now (first message)
            if (isNewSession) {
                safeLogDebug("onSendMessage: creando sesión lazy…")
                when (val created = startChatSessionUseCase()) {
                    is Result.Success -> {
                        effectiveSessionId = created.data.id
                        safeLogDebug("onSendMessage: sesión creada — id=${effectiveSessionId}")
                    }
                    is Result.Error -> {
                        safeLogError("onSendMessage: error al crear sesión — ${created.exception}")
                        _chatState.update { it.copy(isWaitingResponse = false) }
                        _events.send(ChatUiEvent.ShowSnackbar(created.exception.userMessage()))
                        return@launch
                    }
                    else -> return@launch
                }
            }

            val sid = effectiveSessionId ?: return@launch

            safeLogDebug("onSendMessage: enviando mensaje — sid=$sid, hasImage=${imageBase64 != null}, content='${content.take(80)}…'")
            val startTime = System.currentTimeMillis()

            when (val result = sendChatMessageUseCase(sid, content.ifBlank { "📷" }, imageBase64)) {
                is Result.Success -> {
                    val elapsed = System.currentTimeMillis() - startTime
                    safeLogDebug("onSendMessage: SUCCESS en ${elapsed}ms")
                    _chatState.update {
                        it.copy(messages = it.messages + result.data, isWaitingResponse = false)
                    }
                    // Generate title by topic using backend AI (first exchange)
                    if (isFirstMessage) {
                        when (val titleResult = generateChatSessionTitleUseCase(sid)) {
                            is Result.Success -> {
                                _chatState.update { it.copy(sessionTitle = titleResult.data) }
                                safeLogDebug("onSendMessage: título por IA → '${titleResult.data}'")
                            }
                            is Result.Error -> {
                                // Fallback: use truncated first message as title
                                val fallback = generateAutoTitle(content)
                                renameChatSessionUseCase(sid, fallback)
                                _chatState.update { it.copy(sessionTitle = fallback) }
                                safeLogWarn("onSendMessage: error título IA, usando fallback → '$fallback'")
                            }
                            else -> Unit
                        }
                    }
                }
                is Result.Error -> {
                    val elapsed = System.currentTimeMillis() - startTime
                    safeLogError("onSendMessage: ERROR en ${elapsed}ms — ${result.exception}")
                    _chatState.update { it.copy(isWaitingResponse = false) }
                    _events.send(ChatUiEvent.ShowSnackbar(result.exception.userMessage()))
                }
                else -> safeLogWarn("onSendMessage: resultado inesperado")
            }
        }
    }

    /** Generates a readable title from the user's first message (max. 45 chars).*/
    private fun generateAutoTitle(firstMessage: String): String {
        val clean = firstMessage.trim().replace('\n', ' ')
        return if (clean.length <= 45) clean else clean.take(42) + "…"
    }

    /** Archives the active session and issues [ChatUiEvent.NavigateBack] if successful.*/
    fun onArchiveCurrentSession() {
        val sid = effectiveSessionId ?: return
        viewModelScope.launch {
            when (val result = archiveChatSessionUseCase(sid)) {
                is Result.Success -> {
                    _events.send(ChatUiEvent.ShowSnackbar("Sesión archivada"))
                    _events.send(ChatUiEvent.NavigateBack)
                }
                is Result.Error -> {
                    _events.send(ChatUiEvent.ShowSnackbar(result.exception.userMessage()))
                }
                else -> Unit
            }
        }
    }

    companion object {
        private const val MAX_IMAGE_PX = 800   // max. dimension in pixels
        private const val IMAGE_QUALITY = 70   // calidad JPEG (0-100)
    }

    private fun safeLogDebug(message: String) {
        runCatching { android.util.Log.d("AIFIT_DEBUG", message) }
    }

    private fun safeLogWarn(message: String) {
        runCatching { android.util.Log.w("AIFIT_DEBUG", message) }
    }

    private fun safeLogError(message: String) {
        runCatching { android.util.Log.e("AIFIT_DEBUG", message) }
    }

    private fun AppException.userMessage(): String = when (this) {
        is AppException.NetworkException -> "Sin conexión. Comprueba tu internet."
        is AppException.UnauthorizedException -> "Sesión expirada. Vuelve a iniciar sesión."
        is AppException.ForbiddenException -> "No tienes permisos para realizar esta acción."
        is AppException.NotFoundException -> "No se encontró $resource."
        is AppException.ValidationException -> errors.values.firstOrNull() ?: "Datos inválidos."
        is AppException.ConflictException -> "El recurso ya existe o hay un conflicto."
        is AppException.ServerException -> "Error del servidor. Inténtalo más tarde."
        is AppException.AiOverloadedException -> AppException.AI_OVERLOADED_MESSAGE
        is AppException.UnknownException -> message.ifBlank { "Error inesperado. Inténtalo de nuevo." }
        is AppException.InsufficientDataException -> "Necesitas más datos para realizar este análisis. Registra al menos 2 semanas de peso y entrenamientos."
    }
}
