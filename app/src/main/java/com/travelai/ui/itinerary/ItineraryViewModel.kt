package com.travelai.ui.itinerary

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelai.data.model.BudgetCategory
import com.travelai.data.model.BudgetItem
import com.travelai.data.model.ChecklistItem
import com.travelai.data.model.TripPlanDay
import com.travelai.data.model.TripPlanPeriod
import com.travelai.data.model.TripPlanPeriodType
import com.travelai.data.model.TripProfile
import com.travelai.data.model.WeatherDay
import com.travelai.data.model.parseBudgetAmount
import com.travelai.data.parser.ItineraryParser
import com.travelai.data.repository.ChatRepository
import com.travelai.data.repository.WeatherRepository
import com.travelai.ui.share.TripPdfExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ItineraryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val weatherRepository: WeatherRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(ItineraryUiState(isLoading = true))
    val uiState: StateFlow<ItineraryUiState> = _uiState.asStateFlow()

    private val requestedSessionId: Long? =
        savedStateHandle.get<Long>(SESSION_ID_ARG)
            ?: savedStateHandle.get<String>(SESSION_ID_ARG)?.toLongOrNull()

    init {
        loadItinerary()
    }

    fun loadItinerary() {
        val sessionId = requestedSessionId
        if (sessionId == null || sessionId <= 0L) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Không tìm thấy chuyến đi."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    sessionId = sessionId,
                    isLoading = true,
                    errorMessage = null
                )
            }

            runCatching {
                chatRepository.loadSession(sessionId)
                    ?: throw IllegalArgumentException("Không tìm thấy chuyến đi.")
            }.onSuccess { session ->
                val snapshot = session.tripPlanSnapshot
                val fallbackText = snapshot?.rawResponse
                    ?.takeIf { it.isNotBlank() }
                    ?: session.messages
                        .lastOrNull { it.role == ROLE_ASSISTANT }
                        ?.content
                        .orEmpty()

                // Re-parse on load: snapshot.days may be stale if parser was
                // updated after the snapshot was saved (e.g., new AI formats).
                val storedDays = snapshot?.days.orEmpty()
                val days = if (storedDays.isNotEmpty()) {
                    storedDays
                } else {
                    ItineraryParser.parseDays(fallbackText)
                }

                val destination = session.tripProfile?.destination.orEmpty()
                _uiState.update {
                    it.copy(
                        sessionId = session.id,
                        title = session.title,
                        days = days,
                        rawText = fallbackText,
                        tripProfile = session.tripProfile,
                        budgetLimitVnd = parseBudgetAmount(session.tripProfile?.budget.orEmpty()),
                        budgetItems = session.budgetItems,
                        checklistItems = session.checklistItems,
                        weatherDestination = destination,
                        isLoading = false,
                        errorMessage = null
                    )
                }

                if (destination.isNotBlank()) {
                    fetchWeather(destination)
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Không thể tải lịch trình."
                    )
                }
            }
        }
    }

    fun onBudgetCategoryChange(category: BudgetCategory) {
        _uiState.update {
            it.copy(
                budgetForm = it.budgetForm.copy(category = category),
                budgetErrorMessage = null
            )
        }
    }

    fun onBudgetTitleChange(title: String) {
        _uiState.update {
            it.copy(
                budgetForm = it.budgetForm.copy(title = title),
                budgetErrorMessage = null
            )
        }
    }

    fun onBudgetAmountChange(amountText: String) {
        _uiState.update {
            it.copy(
                budgetForm = it.budgetForm.copy(amountText = amountText),
                budgetErrorMessage = null
            )
        }
    }

    fun onBudgetNoteChange(note: String) {
        _uiState.update {
            it.copy(
                budgetForm = it.budgetForm.copy(note = note),
                budgetErrorMessage = null
            )
        }
    }

    fun saveBudgetItem() {
        val state = _uiState.value
        val sessionId = state.sessionId ?: requestedSessionId
        if (sessionId == null || sessionId <= 0L) {
            _uiState.update { it.copy(budgetErrorMessage = "Không tìm thấy chuyến đi.") }
            return
        }

        val form = state.budgetForm
        val amountVnd = parseBudgetAmount(form.amountText)
        if (amountVnd == null) {
            _uiState.update { it.copy(budgetErrorMessage = "Nhập chi phí lớn hơn 0.") }
            return
        }

        val title = form.title.trim().ifBlank { form.category.label }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBudgetSaving = true,
                    budgetErrorMessage = null
                )
            }

            runCatching {
                if (form.editingItemId == null) {
                    chatRepository.addBudgetItem(
                        sessionId = sessionId,
                        category = form.category,
                        title = title,
                        amountVnd = amountVnd,
                        note = form.note
                    )
                } else {
                    chatRepository.updateBudgetItem(
                        sessionId = sessionId,
                        itemId = form.editingItemId,
                        category = form.category,
                        title = title,
                        amountVnd = amountVnd,
                        note = form.note
                    )
                }
                chatRepository.getBudgetItems(sessionId)
            }.onSuccess { budgetItems ->
                _uiState.update {
                    it.copy(
                        budgetItems = budgetItems,
                        budgetForm = BudgetFormState(),
                        isBudgetSaving = false,
                        budgetErrorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isBudgetSaving = false,
                        budgetErrorMessage = throwable.message ?: "Không thể lưu budget."
                    )
                }
            }
        }
    }

    fun editBudgetItem(item: BudgetItem) {
        _uiState.update {
            it.copy(
                budgetForm = BudgetFormState(
                    editingItemId = item.id,
                    category = item.category,
                    title = item.title,
                    amountText = item.amountVnd.toString(),
                    note = item.note
                ),
                budgetErrorMessage = null
            )
        }
    }

    fun cancelBudgetEdit() {
        _uiState.update {
            it.copy(
                budgetForm = BudgetFormState(),
                budgetErrorMessage = null
            )
        }
    }

    fun deleteBudgetItem(item: BudgetItem) {
        val sessionId = _uiState.value.sessionId ?: requestedSessionId ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBudgetSaving = true,
                    budgetErrorMessage = null
                )
            }

            runCatching {
                chatRepository.deleteBudgetItem(sessionId = sessionId, itemId = item.id)
                chatRepository.getBudgetItems(sessionId)
            }.onSuccess { budgetItems ->
                _uiState.update {
                    it.copy(
                        budgetItems = budgetItems,
                        budgetForm = if (it.budgetForm.editingItemId == item.id) {
                            BudgetFormState()
                        } else {
                            it.budgetForm
                        },
                        isBudgetSaving = false,
                        budgetErrorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isBudgetSaving = false,
                        budgetErrorMessage = throwable.message ?: "Không thể xóa budget."
                    )
                }
            }
        }
    }

    fun onChecklistTitleChange(title: String) {
        _uiState.update {
            it.copy(
                checklistDraftTitle = title,
                checklistErrorMessage = null
            )
        }
    }

    fun addChecklistItem() {
        val state = _uiState.value
        val sessionId = state.sessionId ?: requestedSessionId
        if (sessionId == null || sessionId <= 0L) {
            _uiState.update { it.copy(checklistErrorMessage = "Không tìm thấy chuyến đi.") }
            return
        }

        val title = state.checklistDraftTitle.trim()
        if (title.isBlank()) {
            _uiState.update { it.copy(checklistErrorMessage = "Nhập việc cần chuẩn bị.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isChecklistSaving = true,
                    checklistErrorMessage = null
                )
            }

            runCatching {
                chatRepository.addChecklistItem(sessionId = sessionId, title = title)
                chatRepository.getChecklistItems(sessionId)
            }.onSuccess { checklistItems ->
                _uiState.update {
                    it.copy(
                        checklistItems = checklistItems,
                        checklistDraftTitle = "",
                        isChecklistSaving = false,
                        checklistErrorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isChecklistSaving = false,
                        checklistErrorMessage = throwable.message ?: "Không thể lưu checklist."
                    )
                }
            }
        }
    }

    fun generateSmartChecklist() {
        val state = _uiState.value
        val sessionId = state.sessionId ?: requestedSessionId
        if (sessionId == null || sessionId <= 0L) {
            _uiState.update { it.copy(checklistErrorMessage = "Không tìm thấy chuyến đi.") }
            return
        }
        if (state.isGeneratingChecklist) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGeneratingChecklist = true,
                    checklistErrorMessage = null
                )
            }

            runCatching {
                val suggestions = chatRepository.generateSmartChecklist(
                    profile = state.tripProfile,
                    days = state.days,
                    weatherDays = state.weatherDays
                )
                val existingTitles = chatRepository.getChecklistItems(sessionId)
                    .map { item -> item.title.trim().lowercase() }
                    .toSet()
                suggestions
                    .filter { suggestion -> suggestion.trim().lowercase() !in existingTitles }
                    .forEach { suggestion ->
                        chatRepository.addChecklistItem(
                            sessionId = sessionId,
                            title = suggestion
                        )
                    }
                chatRepository.getChecklistItems(sessionId)
            }.onSuccess { checklistItems ->
                _uiState.update {
                    it.copy(
                        checklistItems = checklistItems,
                        isGeneratingChecklist = false,
                        checklistErrorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isGeneratingChecklist = false,
                        checklistErrorMessage = throwable.message ?: "Không thể tạo checklist bằng AI."
                    )
                }
            }
        }
    }

    fun toggleChecklistItem(item: ChecklistItem, isChecked: Boolean) {
        val sessionId = _uiState.value.sessionId ?: requestedSessionId ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isChecklistSaving = true,
                    checklistErrorMessage = null
                )
            }

            runCatching {
                chatRepository.updateChecklistItemChecked(
                    sessionId = sessionId,
                    itemId = item.id,
                    isChecked = isChecked
                )
                chatRepository.getChecklistItems(sessionId)
            }.onSuccess { checklistItems ->
                _uiState.update {
                    it.copy(
                        checklistItems = checklistItems,
                        isChecklistSaving = false,
                        checklistErrorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isChecklistSaving = false,
                        checklistErrorMessage = throwable.message ?: "Không thể cập nhật checklist."
                    )
                }
            }
        }
    }

    fun deleteChecklistItem(item: ChecklistItem) {
        val sessionId = _uiState.value.sessionId ?: requestedSessionId ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isChecklistSaving = true,
                    checklistErrorMessage = null
                )
            }

            runCatching {
                chatRepository.deleteChecklistItem(sessionId = sessionId, itemId = item.id)
                chatRepository.getChecklistItems(sessionId)
            }.onSuccess { checklistItems ->
                _uiState.update {
                    it.copy(
                        checklistItems = checklistItems,
                        isChecklistSaving = false,
                        checklistErrorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isChecklistSaving = false,
                        checklistErrorMessage = throwable.message ?: "Không thể xóa checklist."
                    )
                }
            }
        }
    }

    fun openDayEditor(day: TripPlanDay) {
        _uiState.update {
            it.copy(
                dayEditor = DayEditorState.from(day),
                itineraryEditErrorMessage = null
            )
        }
    }

    fun dismissDayEditor() {
        _uiState.update {
            it.copy(
                dayEditor = null,
                itineraryEditErrorMessage = null
            )
        }
    }

    fun onDayEditorTitleChange(title: String) {
        _uiState.update {
            it.copy(dayEditor = it.dayEditor?.copy(title = title))
        }
    }

    fun onDayEditorPeriodChange(
        periodType: TripPlanPeriodType,
        content: String
    ) {
        _uiState.update { state ->
            state.copy(
                dayEditor = state.dayEditor?.copy(
                    periodContents = state.dayEditor.periodContents + (periodType to content)
                )
            )
        }
    }

    fun saveDayEditor() {
        val state = _uiState.value
        val editor = state.dayEditor ?: return
        val sessionId = state.sessionId ?: requestedSessionId ?: return
        val updatedDay = editor.toTripPlanDay()
        val updatedDays = state.days.map { day ->
            if (day.dayNumber == updatedDay.dayNumber) updatedDay else day
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingItineraryEdit = true, itineraryEditErrorMessage = null) }
            runCatching {
                chatRepository.updateTripPlanSnapshot(sessionId, updatedDays)
                    ?: throw IllegalStateException("Không thể lưu lịch trình.")
            }.onSuccess { snapshot ->
                _uiState.update {
                    it.copy(
                        days = snapshot.days,
                        rawText = snapshot.rawResponse,
                        dayEditor = null,
                        isSavingItineraryEdit = false,
                        itineraryEditErrorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSavingItineraryEdit = false,
                        itineraryEditErrorMessage = throwable.message ?: "Không thể lưu lịch trình."
                    )
                }
            }
        }
    }

    fun regenerateDay(dayNumber: Int) {
        val state = _uiState.value
        val sessionId = state.sessionId ?: requestedSessionId ?: return
        if (state.isRegeneratingDayNumber != null) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRegeneratingDayNumber = dayNumber,
                    itineraryEditErrorMessage = null
                )
            }
            runCatching {
                val regeneratedDay = chatRepository.regenerateTripPlanDay(
                    profile = state.tripProfile,
                    currentDays = state.days,
                    dayNumber = dayNumber
                ) ?: throw IllegalStateException("AI chưa tạo được ngày này.")
                val updatedDays = state.days.map { day ->
                    if (day.dayNumber == dayNumber) regeneratedDay else day
                }
                chatRepository.updateTripPlanSnapshot(sessionId, updatedDays)
                    ?: throw IllegalStateException("Không thể lưu ngày đã tạo lại.")
            }.onSuccess { snapshot ->
                _uiState.update {
                    it.copy(
                        days = snapshot.days,
                        rawText = snapshot.rawResponse,
                        isRegeneratingDayNumber = null,
                        itineraryEditErrorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isRegeneratingDayNumber = null,
                        itineraryEditErrorMessage = throwable.message ?: "Không thể tạo lại ngày này."
                    )
                }
            }
        }
    }

    fun onExportPdf() {
        val sessionId = _uiState.value.sessionId ?: requestedSessionId ?: return
        if (_uiState.value.isExportingPdf) return

        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update {
                it.copy(
                    isExportingPdf = true,
                    pdfErrorMessage = null
                )
            }
            val result = runCatching {
                val tripExport = chatRepository.loadTripExport(sessionId)
                    ?: throw IllegalStateException("Không tìm thấy chuyến đi.")
                withContext(Dispatchers.IO) {
                    TripPdfExporter.export(
                        context = appContext,
                        tripExport = tripExport,
                        weatherDays = state.weatherDays,
                        weatherAdvice = state.weatherAdvice
                    )
                }
            }
            result.onSuccess { uri ->
                _uiState.update {
                    it.copy(
                        pdfFileUri = uri,
                        isExportingPdf = false,
                        pdfErrorMessage = null
                    )
                }
            }
            result.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isExportingPdf = false,
                        pdfErrorMessage = throwable.message ?: "Không thể xuất PDF."
                    )
                }
            }
        }
    }

    fun consumePdfFileUri() {
        _uiState.update { it.copy(pdfFileUri = null) }
    }

    fun consumePdfError() {
        _uiState.update { it.copy(pdfErrorMessage = null) }
    }

    private fun fetchWeather(destination: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isWeatherLoading = true) }
            val forecast = runCatching { weatherRepository.getForecastBundle(destination) }
                .getOrNull()
            _uiState.update {
                it.copy(
                    weatherDays = forecast?.days.orEmpty(),
                    weatherAdvice = forecast?.packingAdvice.orEmpty(),
                    weatherDestination = forecast?.destination ?: destination,
                    isWeatherLoading = false
                )
            }
        }
    }

    private companion object {
        const val SESSION_ID_ARG = "sessionId"
        const val ROLE_ASSISTANT = "assistant"
    }
}

data class ItineraryUiState(
    val sessionId: Long? = null,
    val title: String = "",
    val tripProfile: TripProfile? = null,
    val days: List<TripPlanDay> = emptyList(),
    val rawText: String = "",
    val budgetItems: List<BudgetItem> = emptyList(),
    val budgetLimitVnd: Long? = null,
    val budgetForm: BudgetFormState = BudgetFormState(),
    val budgetErrorMessage: String? = null,
    val isBudgetSaving: Boolean = false,
    val checklistItems: List<ChecklistItem> = emptyList(),
    val checklistDraftTitle: String = "",
    val checklistErrorMessage: String? = null,
    val isChecklistSaving: Boolean = false,
    val isGeneratingChecklist: Boolean = false,
    val weatherDays: List<WeatherDay> = emptyList(),
    val weatherAdvice: List<String> = emptyList(),
    val weatherDestination: String = "",
    val isWeatherLoading: Boolean = false,
    val dayEditor: DayEditorState? = null,
    val isSavingItineraryEdit: Boolean = false,
    val isRegeneratingDayNumber: Int? = null,
    val itineraryEditErrorMessage: String? = null,
    val pdfFileUri: Uri? = null,
    val isExportingPdf: Boolean = false,
    val pdfErrorMessage: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class BudgetFormState(
    val editingItemId: Long? = null,
    val category: BudgetCategory = BudgetCategory.FOOD,
    val title: String = "",
    val amountText: String = "",
    val note: String = ""
)

data class DayEditorState(
    val dayNumber: Int,
    val title: String,
    val periodContents: Map<TripPlanPeriodType, String>
) {
    fun toTripPlanDay(): TripPlanDay =
        TripPlanDay(
            dayNumber = dayNumber,
            title = title.trim().ifBlank { "Ngày $dayNumber" },
            periods = TripPlanPeriodType.entries.map { periodType ->
                TripPlanPeriod(
                    period = periodType,
                    content = periodContents[periodType].orEmpty().trim()
                )
            }
        )

    companion object {
        fun from(day: TripPlanDay): DayEditorState =
            DayEditorState(
                dayNumber = day.dayNumber,
                title = day.title,
                periodContents = TripPlanPeriodType.entries.associateWith { periodType ->
                    day.periods.firstOrNull { it.period == periodType }?.content.orEmpty()
                }
            )
    }
}
