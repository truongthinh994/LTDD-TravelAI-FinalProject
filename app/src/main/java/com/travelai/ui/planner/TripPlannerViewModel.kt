package com.travelai.ui.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelai.data.model.TripProfile
import com.travelai.data.prefs.ThemeMode
import com.travelai.data.prefs.UserPreferencesRepository
import com.travelai.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TripPlannerViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TripPlannerUiState())
    val uiState: StateFlow<TripPlannerUiState> = _uiState.asStateFlow()
    val themeMode: StateFlow<ThemeMode> = userPreferencesRepository.preferencesFlow
        .map { it.themeMode }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeMode.SystemDefault
        )

    init {
        refreshLatestSessionId()
    }

    fun onDestinationChange(value: String) = updateField { copy(destination = value, errorMessage = null) }

    fun onDaysChange(value: String) = updateField { copy(days = value.filter(Char::isDigit), errorMessage = null) }

    fun onBudgetChange(value: String) = updateField { copy(budget = value, errorMessage = null) }

    fun onPeopleChange(value: String) = updateField { copy(people = value.filter(Char::isDigit), errorMessage = null) }

    fun onTravelStyleChange(value: String) = updateField { copy(travelStyle = value, errorMessage = null) }

    fun onTransportChange(value: String) = updateField { copy(transport = value, errorMessage = null) }

    fun onNoteChange(value: String) = updateField { copy(note = value, errorMessage = null) }

    fun createTrip() {
        val state = _uiState.value
        if (state.isCreating) return

        val validationError = state.validationError()
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        val profile = state.toTripProfile()
        _uiState.update {
            it.copy(
                errorMessage = null,
                isCreating = true,
                createdSessionId = null
            )
        }

        viewModelScope.launch {
            try {
                val sessionId = chatRepository.createTripSession(profile)
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        createdSessionId = sessionId,
                        latestSessionId = sessionId,
                        errorMessage = null
                    )
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        createdSessionId = null,
                        errorMessage = throwable.message ?: "Không thể tạo chuyến đi. Vui lòng thử lại."
                    )
                }
            }
        }
    }

    fun consumeCreatedSessionId() {
        _uiState.update { it.copy(createdSessionId = null) }
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(
                if (enabled) ThemeMode.Dark else ThemeMode.Light
            )
        }
    }

    private fun refreshLatestSessionId() {
        viewModelScope.launch {
            runCatching { chatRepository.loadLatestSession()?.id }
                .onSuccess { latestSessionId ->
                    _uiState.update { it.copy(latestSessionId = latestSessionId) }
                }
        }
    }

    private fun updateField(reducer: TripPlannerUiState.() -> TripPlannerUiState) {
        _uiState.update {
            it.reducer().copy(createdSessionId = null)
        }
    }
}

data class TripPlannerUiState(
    val destination: String = "",
    val days: String = "3",
    val budget: String = "",
    val people: String = "2",
    val travelStyle: String = "Tự túc cân bằng",
    val transport: String = "Đi bộ, taxi hoặc xe công nghệ",
    val note: String = "",
    val errorMessage: String? = null,
    val isCreating: Boolean = false,
    val createdSessionId: Long? = null,
    val latestSessionId: Long? = null
)

private const val MAX_DAYS = 30
private const val MAX_PEOPLE = 50

private fun TripPlannerUiState.validationError(): String? {
    val daysValue = days.toIntOrNull()
    val peopleValue = people.toIntOrNull()

    return when {
        destination.isBlank() -> "Nhập điểm đến trước khi tạo lịch trình."
        daysValue == null || daysValue <= 0 -> "Số ngày phải lớn hơn 0."
        daysValue > MAX_DAYS -> "Số ngày không vượt quá $MAX_DAYS."
        peopleValue == null || peopleValue <= 0 -> "Số người phải lớn hơn 0."
        peopleValue > MAX_PEOPLE -> "Số người không vượt quá $MAX_PEOPLE."
        else -> null
    }
}

private fun TripPlannerUiState.toTripProfile(): TripProfile = TripProfile(
    destination = destination.trim(),
    days = days.toIntOrNull() ?: 1,
    budget = budget.trim(),
    people = people.toIntOrNull() ?: 1,
    travelStyle = travelStyle.trim(),
    transport = transport.trim(),
    note = note.trim()
)
