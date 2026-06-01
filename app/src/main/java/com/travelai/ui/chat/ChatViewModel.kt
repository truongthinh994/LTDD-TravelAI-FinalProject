package com.travelai.ui.chat

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelai.data.api.DeepSeekMessage
import com.travelai.data.model.TripPlanSnapshot
import com.travelai.data.model.TripProfile
import com.travelai.data.model.toInitialPrompt
import com.travelai.data.model.toPromptContext
import com.travelai.data.repository.ChatRepository
import com.travelai.data.repository.StoredChatMessage
import com.travelai.ui.chat.components.SamplePromptOption
import com.travelai.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentSessionId: Long? = null
    private var currentTripProfile: TripProfile? = null
    private var pendingRetry: PendingRetry? = null
    // True for one auto-started trip-generation send.
    // When the response arrives, ChatScreen jumps to the Itinerary screen.
    private var autoNavToItineraryPending: Boolean = false
    private val requestedSessionId: Long? = savedStateHandle
        .get<String>(SESSION_ID_ARG)
        ?.toLongOrNull()
        ?.takeIf { it > 0 }
    private val draftPrompt: String? = savedStateHandle
        .get<String>(DRAFT_PROMPT_ARG)
        ?.trim()
        ?.takeIf { it.isNotBlank() }
    private val shouldAutoStartTrip: Boolean = savedStateHandle
        .get<Boolean>(AUTO_START_ARG)
        ?: false

    init {
        when {
            // Landmark scanner pre-fills the input — stay on an empty session
            // so the user can edit/send the suggested prompt.
            draftPrompt != null -> _uiState.update { it.copy(inputText = draftPrompt) }
            // Resume an explicit session passed via nav args (Trip Library tap,
            // auto-start trip flow, etc.).
            requestedSessionId != null -> loadInitialSession()
            // No args, no draft: this is a fresh "new chat" tab. Do nothing,
            // so the empty-state with sample chips shows.
        }
    }

    fun onInputChange(inputText: String) {
        pendingRetry = null
        _uiState.update {
            it.copy(
                inputText = inputText,
                errorMessage = null,
                canRetry = false,
                offlineBannerMessage = null
            )
        }
    }

    /**
     * Spin up a brand-new trip-planning session from a sample chip.
     *
     * Mirrors the Trip Planner form flow: creates a [com.travelai.data.db.entities.ChatSessionEntity]
     * with the chip's [SamplePromptOption.profile], attaches the profile to
     * the in-memory state so the system prompt carries trip context, then
     * sends the chip label as the user's first message. The AI therefore
     * receives the same structured guidance the planner form provides and
     * the response renders properly inside the Itinerary screen.
     */
    fun startTripFromSample(option: SamplePromptOption) {
        if (_uiState.value.isLoading || _uiState.value.isStreaming) return

        viewModelScope.launch {
            // Reset any in-progress chat so the new sample session starts clean.
            startNewChat()

            val sessionId = runCatching {
                chatRepository.createTripSession(option.profile)
            }.getOrElse { throwable ->
                _uiState.update {
                    it.copy(
                        errorMessage = throwable.message
                            ?: "Không thể tạo chuyến đi từ gợi ý."
                    )
                }
                return@launch
            }

            currentSessionId = sessionId
            currentTripProfile = option.profile
            _uiState.update {
                it.copy(
                    sessionId = sessionId,
                    inputText = option.label,
                    errorMessage = null,
                    canRetry = false,
                    offlineBannerMessage = null
                )
            }
            sendMessage()
        }
    }

    fun sendMessage() {
        val state = _uiState.value
        val messageText = state.inputText.trim()
        if (messageText.isBlank() || state.isLoading || state.isStreaming) return
        pendingRetry = null

        if (!hasInternetConnection()) {
            _uiState.update {
                it.copy(
                    errorMessage = null,
                    canRetry = false,
                    offlineBannerMessage = OFFLINE_MESSAGE
                )
            }
            return
        }

        val userMessage = ChatMessage(
            role = ChatRole.USER,
            content = messageText
        )
        val updatedMessages = state.messages + userMessage

        _uiState.update {
            it.copy(
                messages = updatedMessages,
                inputText = "",
                isLoading = true,
                isStreaming = false,
                errorMessage = null,
                canRetry = false,
                offlineBannerMessage = null
            )
        }

        viewModelScope.launch {
            var retryCandidate: PendingRetry? = null

            try {
                val sessionId = getOrCreateSessionId(messageText)
                chatRepository.saveMessage(
                    sessionId = sessionId,
                    role = ROLE_USER,
                    content = messageText
                )

                val deepSeekMessages = buildDeepSeekMessages(updatedMessages)
                val keepRawSnapshotWhenUnparsed = shouldKeepRawSnapshotWhenUnparsed(updatedMessages)
                retryCandidate = PendingRetry(
                    sessionId = sessionId,
                    messages = deepSeekMessages,
                    keepRawSnapshotWhenUnparsed = keepRawSnapshotWhenUnparsed
                )

                val response = streamAssistantResponse(deepSeekMessages)
                chatRepository.saveMessage(
                    sessionId = sessionId,
                    role = ROLE_ASSISTANT,
                    content = response
                )
                val tripPlanSnapshot = saveTripPlanSnapshotIfUseful(
                    sessionId = sessionId,
                    rawResponse = response,
                    keepRawWhenUnparsed = keepRawSnapshotWhenUnparsed
                )

                val triggerAutoNav = autoNavToItineraryPending
                autoNavToItineraryPending = false
                _uiState.update {
                    it.copy(
                        sessionId = sessionId,
                        tripPlanSnapshot = tripPlanSnapshot ?: it.tripPlanSnapshot,
                        isLoading = false,
                        isStreaming = false,
                        errorMessage = null,
                        canRetry = false,
                        offlineBannerMessage = null,
                        navigateToItinerarySessionId = if (triggerAutoNav) sessionId else it.navigateToItinerarySessionId
                    )
                }
                pendingRetry = null
            } catch (throwable: Throwable) {
                // If auto-nav was pending and the send failed, clear it so the
                // user can read the error in chat instead of being navigated away.
                autoNavToItineraryPending = false
                rollbackStreamingPlaceholderIfEmpty()
                handleSendFailure(
                    throwable = throwable,
                    retryCandidate = retryCandidate
                )
            }
        }
    }

    fun retryLastMessage() {
        val retry = pendingRetry ?: return
        if (_uiState.value.isLoading || _uiState.value.isStreaming) return

        if (!hasInternetConnection()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    offlineBannerMessage = OFFLINE_MESSAGE
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                isStreaming = false,
                errorMessage = null,
                canRetry = false,
                offlineBannerMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val response = streamAssistantResponse(retry.messages)
                chatRepository.saveMessage(
                    sessionId = retry.sessionId,
                    role = ROLE_ASSISTANT,
                    content = response
                )
                val tripPlanSnapshot = saveTripPlanSnapshotIfUseful(
                    sessionId = retry.sessionId,
                    rawResponse = response,
                    keepRawWhenUnparsed = retry.keepRawSnapshotWhenUnparsed
                )

                _uiState.update {
                    it.copy(
                        sessionId = retry.sessionId,
                        tripPlanSnapshot = tripPlanSnapshot ?: it.tripPlanSnapshot,
                        isLoading = false,
                        isStreaming = false,
                        errorMessage = null,
                        canRetry = false,
                        offlineBannerMessage = null
                    )
                }
                pendingRetry = null
            } catch (throwable: Throwable) {
                rollbackStreamingPlaceholderIfEmpty()
                handleSendFailure(
                    throwable = throwable,
                    retryCandidate = retry
                )
            }
        }
    }

    fun shareCurrentSession() {
        val sessionId = currentSessionId ?: _uiState.value.sessionId ?: return
        if (_uiState.value.isSharing) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSharing = true,
                    errorMessage = null
                )
            }

            runCatching {
                chatRepository.createTripExportText(sessionId)
                    ?.takeIf { it.isNotBlank() }
                    ?: throw IllegalStateException("Chưa có nội dung để chia sẻ.")
            }.onSuccess { shareText ->
                _uiState.update {
                    it.copy(
                        shareText = shareText,
                        isSharing = false
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSharing = false,
                        errorMessage = throwable.message ?: "Không thể tạo nội dung chia sẻ."
                    )
                }
            }
        }
    }

    fun consumeShareText() {
        _uiState.update { it.copy(shareText = null) }
    }

    /**
     * Reset to a clean empty chat. Called when the user taps the Chat bottom
     * nav from any screen — including from the chat tab itself — so "Chat"
     * is a true "new chat" affordance. Old conversations remain accessible
     * via the History (Trip Library) screen.
     */
    fun startNewChat() {
        currentSessionId = null
        currentTripProfile = null
        pendingRetry = null
        autoNavToItineraryPending = false
        _uiState.value = ChatUiState()
    }

    private fun loadInitialSession() {
        val sessionId = requestedSessionId ?: return
        viewModelScope.launch {
            runCatching {
                chatRepository.loadSession(sessionId)
            }.onSuccess { session ->
                if (
                    session != null &&
                    currentSessionId == null &&
                    _uiState.value.messages.isEmpty() &&
                    !_uiState.value.isLoading
                ) {
                    val loadedMessages = session.messages.mapNotNull { message ->
                        message.toChatMessage()
                    }
                    currentSessionId = session.id
                    currentTripProfile = session.tripProfile
                    _uiState.update {
                        it.copy(
                            sessionId = session.id,
                            messages = loadedMessages,
                            tripPlanSnapshot = session.tripPlanSnapshot,
                            errorMessage = null,
                            canRetry = false,
                            offlineBannerMessage = null
                        )
                    }
                    if (
                        shouldAutoStartTrip &&
                        loadedMessages.isEmpty() &&
                        session.tripProfile != null
                    ) {
                        startTripFromProfile(session.tripProfile)
                    }
                }
            }.onFailure { throwable ->
                val chatError = throwable.toChatError()
                _uiState.update {
                    it.copy(
                        errorMessage = chatError.message,
                        canRetry = false,
                        offlineBannerMessage = chatError.offlineBannerMessage
                    )
                }
            }
        }
    }

    private fun startTripFromProfile(profile: TripProfile) {
        if (_uiState.value.messages.isNotEmpty() || _uiState.value.isLoading) return
        autoNavToItineraryPending = true
        _uiState.update { it.copy(inputText = profile.toInitialPrompt()) }
        sendMessage()
    }

    fun consumeNavigateToItinerary() {
        _uiState.update { it.copy(navigateToItinerarySessionId = null) }
    }

    /**
     * Collect the streaming response into the UI state.
     *
     * - The first non-empty chunk transitions the UI from "loading dots" to
     *   "streaming bubble": a new assistant message is appended with the
     *   chunk content, and `isLoading=false / isStreaming=true` is set.
     * - Each subsequent chunk replaces the placeholder content with the
     *   accumulated text so the user sees text grow word-by-word.
     * - The final accumulated text is returned for DB persistence by callers.
     */
    private suspend fun streamAssistantResponse(messages: List<DeepSeekMessage>): String {
        val builder = StringBuilder()
        var assistantIndex = -1
        chatRepository.streamMessage(messages).collect { chunk ->
            builder.append(chunk)
            val currentText = builder.toString()
            _uiState.update { state ->
                if (assistantIndex == -1) {
                    val newMessages = state.messages + ChatMessage(
                        role = ChatRole.ASSISTANT,
                        content = currentText
                    )
                    assistantIndex = newMessages.size - 1
                    state.copy(
                        messages = newMessages,
                        isLoading = false,
                        isStreaming = true
                    )
                } else {
                    val idxSnapshot = assistantIndex
                    state.copy(
                        messages = state.messages.mapIndexed { idx, msg ->
                            if (idx == idxSnapshot) msg.copy(content = currentText) else msg
                        }
                    )
                }
            }
        }
        val finalResponse = builder.toString()
        if (finalResponse.isBlank()) {
            throw IllegalStateException("DeepSeek không trả về nội dung.")
        }
        return finalResponse
    }

    /**
     * If the stream failed mid-way, drop the partial assistant placeholder so
     * the user sees a clean "Thử lại" affordance instead of a half-finished
     * bubble next to an error banner.
     */
    private fun rollbackStreamingPlaceholderIfEmpty() {
        _uiState.update { state ->
            val last = state.messages.lastOrNull()
            if (state.isStreaming && last?.role == ChatRole.ASSISTANT) {
                state.copy(
                    messages = state.messages.dropLast(1),
                    isStreaming = false
                )
            } else {
                state.copy(isStreaming = false)
            }
        }
    }

    private suspend fun saveTripPlanSnapshotIfUseful(
        sessionId: Long,
        rawResponse: String,
        keepRawWhenUnparsed: Boolean
    ): TripPlanSnapshot? = chatRepository.saveTripPlanSnapshot(
        sessionId = sessionId,
        rawResponse = rawResponse,
        keepRawWhenUnparsed = keepRawWhenUnparsed
    )

    private fun handleSendFailure(
        throwable: Throwable,
        retryCandidate: PendingRetry?
    ) {
        if (throwable is CancellationException && throwable !is TimeoutCancellationException) {
            throw throwable
        }

        val chatError = throwable.toChatError()
        pendingRetry = if (chatError.canRetry) retryCandidate else null

        _uiState.update {
            it.copy(
                isLoading = false,
                isStreaming = false,
                errorMessage = chatError.message,
                canRetry = chatError.canRetry,
                offlineBannerMessage = chatError.offlineBannerMessage
            )
        }
    }

    private suspend fun getOrCreateSessionId(firstMessage: String): Long {
        currentSessionId?.let { return it }
        return chatRepository.createSession(firstMessage).also { sessionId ->
            currentSessionId = sessionId
            _uiState.update { it.copy(sessionId = sessionId) }
        }
    }

    private fun buildDeepSeekMessages(messages: List<ChatMessage>): List<DeepSeekMessage> {
        val conversationMessages = messages.map { it.toDeepSeekMessage() }
        val systemPrompt = currentTripProfile?.let { profile ->
            Constants.SYSTEM_PROMPT + "\n\n" + profile.toPromptContext()
        } ?: Constants.SYSTEM_PROMPT

        return listOf(
            DeepSeekMessage(
                role = ROLE_SYSTEM,
                content = systemPrompt
            )
        ) + trimContextMessages(conversationMessages, systemPrompt.length)
    }

    private fun trimContextMessages(
        messages: List<DeepSeekMessage>,
        systemPromptLength: Int
    ): List<DeepSeekMessage> {
        var totalChars = systemPromptLength
        val keptReversed = mutableListOf<DeepSeekMessage>()

        for (message in messages.asReversed()) {
            val messageChars = message.role.length + message.content.length
            if (keptReversed.isEmpty() || totalChars + messageChars <= Constants.MAX_CONTEXT_CHARS) {
                keptReversed += message
                totalChars += messageChars
            }
        }

        return keptReversed.asReversed()
    }

    private fun shouldKeepRawSnapshotWhenUnparsed(messages: List<ChatMessage>): Boolean =
        currentTripProfile != null &&
            messages.count { it.role == ChatRole.USER } == 1 &&
            messages.none { it.role == ChatRole.ASSISTANT }

    private fun ChatMessage.toDeepSeekMessage(): DeepSeekMessage = DeepSeekMessage(
        role = when (role) {
            ChatRole.USER -> ROLE_USER
            ChatRole.ASSISTANT -> ROLE_ASSISTANT
        },
        content = content
    )

    private fun StoredChatMessage.toChatMessage(): ChatMessage? {
        val chatRole = when (role) {
            ROLE_USER -> ChatRole.USER
            ROLE_ASSISTANT -> ChatRole.ASSISTANT
            else -> return null
        }
        return ChatMessage(
            role = chatRole,
            content = content
        )
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun Throwable.toChatError(): ChatError = when (this) {
        is TimeoutCancellationException,
        is SocketTimeoutException -> ChatError(
            message = TIMEOUT_MESSAGE,
            canRetry = true
        )

        is IOException -> {
            val offlineBannerMessage = if (!hasInternetConnection()) OFFLINE_MESSAGE else null
            ChatError(
                message = offlineBannerMessage ?: "Kết nối không ổn định. Vui lòng thử lại.",
                canRetry = false,
                offlineBannerMessage = offlineBannerMessage
            )
        }

        is HttpException -> ChatError(
            message = toHttpUserMessage(),
            canRetry = false
        )

        else -> ChatError(
            message = message ?: "Đã có lỗi xảy ra. Vui lòng thử lại.",
            canRetry = false
        )
    }

    private fun HttpException.toHttpUserMessage(): String = when (code()) {
        401 -> "DeepSeek từ chối API key (401). Kiểm tra DEEPSEEK_API_KEY."
        403 -> "DeepSeek không cho phép truy cập (403). Kiểm tra quyền API key."
        429 -> "DeepSeek đang giới hạn lượt gọi (429). Vui lòng thử lại sau."
        in 500..599 -> "DeepSeek đang lỗi máy chủ (${code()}). Vui lòng thử lại sau."
        else -> "DeepSeek trả về lỗi ${code()}. Vui lòng thử lại."
    }
}

data class ChatUiState(
    val sessionId: Long? = null,
    val messages: List<ChatMessage> = emptyList(),
    val tripPlanSnapshot: TripPlanSnapshot? = null,
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isStreaming: Boolean = false,
    val errorMessage: String? = null,
    val canRetry: Boolean = false,
    val offlineBannerMessage: String? = null,
    val shareText: String? = null,
    val isSharing: Boolean = false,
    val navigateToItinerarySessionId: Long? = null
)

data class ChatMessage(
    val role: ChatRole,
    val content: String
)

enum class ChatRole {
    USER,
    ASSISTANT
}

private data class PendingRetry(
    val sessionId: Long,
    val messages: List<DeepSeekMessage>,
    val keepRawSnapshotWhenUnparsed: Boolean
)

private data class ChatError(
    val message: String?,
    val canRetry: Boolean,
    val offlineBannerMessage: String? = null
)

private const val ROLE_SYSTEM = "system"
private const val ROLE_USER = "user"
private const val ROLE_ASSISTANT = "assistant"
private const val SESSION_ID_ARG = "sessionId"
private const val DRAFT_PROMPT_ARG = "draftPrompt"
private const val AUTO_START_ARG = "autoStart"
private const val TIMEOUT_MESSAGE = "Không phản hồi, thử lại?"
private const val OFFLINE_MESSAGE = "Không có kết nối internet. Kiểm tra mạng rồi thử lại."
