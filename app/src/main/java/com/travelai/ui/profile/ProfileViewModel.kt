package com.travelai.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelai.data.prefs.AvatarStyle
import com.travelai.data.prefs.UserPreferencesRepository
import com.travelai.data.repository.ChatRepository
import com.travelai.data.repository.LandmarkScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val chatRepository: ChatRepository,
    landmarkScanRepository: LandmarkScanRepository
) : ViewModel() {
    private val tripCount = MutableStateFlow(0)
    private val editState = MutableStateFlow(ProfileEditState())
    private val scanCount = landmarkScanRepository.observeHistory().map { it.size }

    val uiState: StateFlow<ProfileUiState> = combine(
        userPreferencesRepository.preferencesFlow,
        tripCount,
        scanCount,
        editState
    ) { prefs, trips, scans, edit ->
        ProfileUiState(
            displayName = prefs.displayName,
            avatarStyle = prefs.avatarStyle,
            tripCount = trips,
            scanCount = scans,
            isEditingProfile = edit.isEditingProfile,
            editDraft = edit.editDraft,
            editAvatarStyle = edit.editAvatarStyle
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ProfileUiState()
    )

    init {
        viewModelScope.launch {
            try {
                tripCount.value = chatRepository.getSessions().size
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                tripCount.value = 0
            }
        }
    }

    fun startEditingProfile() {
        editState.update {
            it.copy(
                isEditingProfile = true,
                editDraft = uiState.value.displayName,
                editAvatarStyle = uiState.value.avatarStyle
            )
        }
    }

    fun dismissProfileEditor() {
        editState.value = ProfileEditState()
    }

    fun onEditDraftChange(value: String) {
        editState.update { it.copy(editDraft = value) }
    }

    fun onEditAvatarStyleChange(style: AvatarStyle) {
        editState.update { it.copy(editAvatarStyle = style) }
    }

    fun saveProfile() {
        val edit = editState.value
        viewModelScope.launch {
            userPreferencesRepository.setProfile(edit.editDraft, edit.editAvatarStyle)
            editState.value = ProfileEditState()
        }
    }
}

data class ProfileUiState(
    val displayName: String = "",
    val avatarStyle: AvatarStyle = AvatarStyle.Aurora,
    val tripCount: Int = 0,
    val scanCount: Int = 0,
    val isEditingProfile: Boolean = false,
    val editDraft: String = "",
    val editAvatarStyle: AvatarStyle = AvatarStyle.Aurora
)

private data class ProfileEditState(
    val isEditingProfile: Boolean = false,
    val editDraft: String = "",
    val editAvatarStyle: AvatarStyle = AvatarStyle.Aurora
)
