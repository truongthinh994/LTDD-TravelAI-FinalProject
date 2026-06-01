package com.travelai.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelai.data.repository.ChatRepository
import com.travelai.data.repository.StoredChatSessionSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState(isLoading = true))
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var allSessions: List<HistorySession> = emptyList()

    init {
        loadSessions()
    }

    fun loadSessions() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            runCatching {
                chatRepository.getSessions().map { session ->
                    session.toHistorySession()
                }
            }.onSuccess { sessions ->
                allSessions = sessions
                _uiState.update {
                    it.copy(
                        sessions = sessions.filteredByTitle(it.searchQuery),
                        isLoading = false
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Không thể tải thư viện chuyến đi."
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                sessions = allSessions.filteredByTitle(query)
            )
        }
    }

    fun startRenameSession(session: HistorySession) {
        _uiState.update {
            it.copy(
                renamingSession = session,
                renameTitle = session.title,
                renameErrorMessage = null
            )
        }
    }

    fun onRenameTitleChange(title: String) {
        _uiState.update {
            it.copy(
                renameTitle = title,
                renameErrorMessage = null
            )
        }
    }

    fun dismissRenameSession() {
        if (_uiState.value.isSavingSession) return
        _uiState.update {
            it.copy(
                renamingSession = null,
                renameTitle = "",
                renameErrorMessage = null
            )
        }
    }

    fun confirmRenameSession() {
        val state = _uiState.value
        val session = state.renamingSession ?: return
        val cleanTitle = state.renameTitle.trim()
        if (cleanTitle.isBlank()) {
            _uiState.update { it.copy(renameErrorMessage = "Tên chuyến đi không được để trống.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingSession = true) }
            runCatching {
                chatRepository.renameSession(session.id, cleanTitle)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isSavingSession = false,
                        renamingSession = null,
                        renameTitle = "",
                        renameErrorMessage = null
                    )
                }
                loadSessions()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSavingSession = false,
                        renameErrorMessage = throwable.message ?: "Không thể đổi tên chuyến đi."
                    )
                }
            }
        }
    }

    fun requestDeleteSession(session: HistorySession) {
        _uiState.update { it.copy(deletingSession = session) }
    }

    fun dismissDeleteSession() {
        if (_uiState.value.isSavingSession) return
        _uiState.update { it.copy(deletingSession = null) }
    }

    fun confirmDeleteSession() {
        val session = _uiState.value.deletingSession ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingSession = true) }
            runCatching {
                chatRepository.deleteSession(session.id)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isSavingSession = false,
                        deletingSession = null
                    )
                }
                loadSessions()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSavingSession = false,
                        errorMessage = throwable.message ?: "Không thể xóa chuyến đi."
                    )
                }
            }
        }
    }

    fun togglePinned(session: HistorySession) {
        viewModelScope.launch {
            runCatching {
                chatRepository.updateSessionPinned(
                    sessionId = session.id,
                    isPinned = !session.isPinned
                )
            }.onSuccess {
                loadSessions()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "Không thể cập nhật ghim.")
                }
            }
        }
    }

    fun shareSession(session: HistorySession) {
        if (_uiState.value.sharingSessionId != null) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    sharingSessionId = session.id,
                    errorMessage = null
                )
            }
            runCatching {
                chatRepository.createTripExportText(session.id)
                    ?.takeIf { it.isNotBlank() }
                    ?: throw IllegalStateException("Chưa có nội dung để chia sẻ.")
            }.onSuccess { shareText ->
                _uiState.update {
                    it.copy(
                        sharingSessionId = null,
                        shareText = shareText
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        sharingSessionId = null,
                        errorMessage = throwable.message ?: "Không thể tạo nội dung chia sẻ."
                    )
                }
            }
        }
    }

    fun consumeShareText() {
        _uiState.update { it.copy(shareText = null) }
    }

    private fun StoredChatSessionSummary.toHistorySession(): HistorySession =
        HistorySession(
            id = id,
            title = title,
            createdAtText = formatTimestamp(createdAt),
            isPinned = isPinned
        )

    private fun formatTimestamp(timestamp: Long): String =
        Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .format(HISTORY_DATE_FORMATTER)
}

data class HistoryUiState(
    val sessions: List<HistorySession> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val renamingSession: HistorySession? = null,
    val renameTitle: String = "",
    val renameErrorMessage: String? = null,
    val deletingSession: HistorySession? = null,
    val isSavingSession: Boolean = false,
    val sharingSessionId: Long? = null,
    val shareText: String? = null
) {
    val hasSearchQuery: Boolean
        get() = searchQuery.isNotBlank()
}

data class HistorySession(
    val id: Long,
    val title: String,
    val createdAtText: String,
    val isPinned: Boolean
)

private fun List<HistorySession>.filteredByTitle(query: String): List<HistorySession> {
    val cleanQuery = query.trim()
    if (cleanQuery.isBlank()) return this
    return filter { session ->
        session.title.contains(cleanQuery, ignoreCase = true)
    }
}

private val HISTORY_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault())
