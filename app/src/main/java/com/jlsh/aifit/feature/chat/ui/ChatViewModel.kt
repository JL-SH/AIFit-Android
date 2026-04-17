package com.jlsh.aifit.feature.chat.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
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
    val listState: StateFlow<ChatListUiState> = _listState.asStateFlow()

    // IDs pendientes de borrado en red (para filtrarlos en loadSessions)
    private val pendingDeleteIds = mutableSetOf<String>()

    // ── Active Chat State ────────────────────────────────────────────────────
    private val _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    // ── Events ───────────────────────────────────────────────────────────────
    private val _events = Channel<ChatUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Puede ser null cuando el usuario navega a "nuevo chat" sin haber enviado nada aún.
    // Se asigna el id real en el primer envío.
    private val savedSessionId: String? = savedStateHandle.get<String>("sessionId")
    private var effectiveSessionId: String? = savedSessionId

    init {
        if (savedSessionId != null) {
            loadSession(savedSessionId)
        } else {
            // Chat nuevo: mostrar pantalla vacía lista para escribir
            _chatState.value = ChatState(isLoading = false)
            loadSessions()
        }
    }

    // ── Session List ─────────────────────────────────────────────────────────

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
                        _listState.value = ChatListUiState.Error(result.exception.toMessage())
                    }
                    is Result.Loading -> {
                        _listState.value = ChatListUiState.Loading
                    }
                }
            }
        }
    }

    fun onNewSession() {
        // Navegar a chat nuevo SIN llamar al backend todavía
        viewModelScope.launch {
            _events.send(ChatUiEvent.NavigateToNewChat)
        }
    }

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
                    _events.send(ChatUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    fun onArchiveSession(id: String) {
        viewModelScope.launch {
            when (val result = archiveChatSessionUseCase(id)) {
                is Result.Success -> {
                    loadSessions()
                    _events.send(ChatUiEvent.ShowSnackbar("Sesión archivada"))
                }
                is Result.Error -> {
                    _events.send(ChatUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    fun onRenameSession(id: String, newTitle: String) {
        viewModelScope.launch {
            when (val result = renameChatSessionUseCase(id, newTitle.trim())) {
                is Result.Success -> {
                    loadSessions()
                    _events.send(ChatUiEvent.ShowSnackbar("Conversación renombrada"))
                }
                is Result.Error -> {
                    _events.send(ChatUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    // ── Active Chat ──────────────────────────────────────────────────────────

    fun loadSession(id: String) {
        viewModelScope.launch {
            _chatState.update { it.copy(isLoading = true, error = null) }
            getChatSessionUseCase(id).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _chatState.update {
                            it.copy(
                                messages = result.data.messages,
                                sessionTitle = result.data.title,
                                isLoading = false,
                                error = null,
                            )
                        }
                    }
                    is Result.Error -> {
                        _chatState.update {
                            it.copy(isLoading = false, error = result.exception.toMessage())
                        }
                    }
                    is Result.Loading -> {
                        _chatState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    fun onInputChanged(text: String) {
        _chatState.update { it.copy(inputText = text) }
    }

    fun onImageSelected(rawBytes: ByteArray) {
        viewModelScope.launch(Dispatchers.Default) {
            val compressed = compressImageBytes(rawBytes)
            Log.d("AIFIT_DEBUG", "onImageSelected: raw=${rawBytes.size / 1024}KB → compressed=${compressed.size / 1024}KB")
            _chatState.update { it.copy(pendingImageBytes = compressed) }
        }
    }

    /**
     * Decodifica los bytes en un Bitmap, lo escala a máx. MAX_IMAGE_PX × MAX_IMAGE_PX
     * y lo recomprime como JPEG al 70 % de calidad.
     * Resultado típico: foto de 4 MB → ~80-120 KB → Base64 ~160 KB (viable en HTTP JSON).
     */
    private fun compressImageBytes(raw: ByteArray): ByteArray {
        val original = BitmapFactory.decodeByteArray(raw, 0, raw.size)
            ?: return raw  // fallback: si no se puede decodificar, usar raw

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

    fun onClearPendingImage() {
        _chatState.update { it.copy(pendingImageBytes = null) }
    }

    fun onSendMessage() {
        val content = _chatState.value.inputText.trim()
        val imageBytes = _chatState.value.pendingImageBytes
        if ((content.isBlank() && imageBytes == null) || content.length > 4000) {
            Log.w("AIFIT_DEBUG", "onSendMessage: contenido inválido (blank=${content.isBlank()}, hasImage=${imageBytes != null}, len=${content.length})")
            return
        }

        // ¿Es el primer mensaje de una sesión nueva (aún sin ID)?
        val isNewSession = effectiveSessionId == null
        val isFirstMessage = _chatState.value.messages.isEmpty()

        // Convertir imagen a Base64 si existe
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
            // Si todavía no hay sesión, crearla ahora (primer mensaje)
            if (isNewSession) {
                Log.d("AIFIT_DEBUG", "onSendMessage: creando sesión lazy…")
                when (val created = startChatSessionUseCase()) {
                    is Result.Success -> {
                        effectiveSessionId = created.data.id
                        Log.d("AIFIT_DEBUG", "onSendMessage: sesión creada — id=${effectiveSessionId}")
                    }
                    is Result.Error -> {
                        Log.e("AIFIT_DEBUG", "onSendMessage: error al crear sesión — ${created.exception}")
                        _chatState.update { it.copy(isWaitingResponse = false) }
                        _events.send(ChatUiEvent.ShowSnackbar(created.exception.toMessage()))
                        return@launch
                    }
                    else -> return@launch
                }
            }

            val sid = effectiveSessionId ?: return@launch

            Log.d("AIFIT_DEBUG", "onSendMessage: enviando mensaje — sid=$sid, hasImage=${imageBase64 != null}, content='${content.take(80)}…'")
            val startTime = System.currentTimeMillis()

            when (val result = sendChatMessageUseCase(sid, content.ifBlank { "📷" }, imageBase64)) {
                is Result.Success -> {
                    val elapsed = System.currentTimeMillis() - startTime
                    Log.d("AIFIT_DEBUG", "onSendMessage: SUCCESS en ${elapsed}ms")
                    _chatState.update {
                        it.copy(messages = it.messages + result.data, isWaitingResponse = false)
                    }
                    // Generar título por temática usando IA del backend (primer intercambio)
                    if (isFirstMessage) {
                        when (val titleResult = generateChatSessionTitleUseCase(sid)) {
                            is Result.Success -> {
                                _chatState.update { it.copy(sessionTitle = titleResult.data) }
                                Log.d("AIFIT_DEBUG", "onSendMessage: título por IA → '${titleResult.data}'")
                            }
                            is Result.Error -> {
                                // Fallback: usar el primer mensaje truncado
                                val fallback = generateAutoTitle(content)
                                renameChatSessionUseCase(sid, fallback)
                                _chatState.update { it.copy(sessionTitle = fallback) }
                                Log.w("AIFIT_DEBUG", "onSendMessage: error título IA, usando fallback → '$fallback'")
                            }
                            else -> Unit
                        }
                    }
                }
                is Result.Error -> {
                    val elapsed = System.currentTimeMillis() - startTime
                    Log.e("AIFIT_DEBUG", "onSendMessage: ERROR en ${elapsed}ms — ${result.exception}")
                    _chatState.update { it.copy(isWaitingResponse = false) }
                    _events.send(ChatUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Log.w("AIFIT_DEBUG", "onSendMessage: resultado inesperado")
            }
        }
    }

    /** Genera un título legible a partir del primer mensaje del usuario (máx. 45 chars). */
    private fun generateAutoTitle(firstMessage: String): String {
        val clean = firstMessage.trim().replace('\n', ' ')
        return if (clean.length <= 45) clean else clean.take(42) + "…"
    }

    fun onArchiveCurrentSession() {
        val sid = effectiveSessionId ?: return
        viewModelScope.launch {
            when (val result = archiveChatSessionUseCase(sid)) {
                is Result.Success -> {
                    _events.send(ChatUiEvent.ShowSnackbar("Sesión archivada"))
                    _events.send(ChatUiEvent.NavigateBack)
                }
                is Result.Error -> {
                    _events.send(ChatUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    companion object {
        private const val MAX_IMAGE_PX = 800   // máx. dimensión en píxeles
        private const val IMAGE_QUALITY = 70   // calidad JPEG (0-100)
    }
}
