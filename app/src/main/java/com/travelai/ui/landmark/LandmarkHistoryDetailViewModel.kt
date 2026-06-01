package com.travelai.ui.landmark

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelai.data.model.LandmarkScan
import com.travelai.data.repository.LandmarkScanRepository
import com.travelai.ui.navigation.TravelAiRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LandmarkHistoryDetailViewModel @Inject constructor(
    private val repository: LandmarkScanRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val scanId: Long = checkNotNull(savedStateHandle[TravelAiRoutes.SCAN_ID_ARG]) {
        "scanId nav argument is missing"
    }

    private val _state = MutableStateFlow(LandmarkHistoryDetailState())
    val state: StateFlow<LandmarkHistoryDetailState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val scan = repository.getById(scanId)
                val bitmap = scan?.imagePath?.let { path ->
                    withContext(Dispatchers.IO) {
                        runCatching { BitmapFactory.decodeFile(path) }.getOrNull()
                    }
                }
                scan to bitmap
            }.onSuccess { (scan, bitmap) ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        scan = scan,
                        bitmap = bitmap,
                        errorMessage = if (scan == null) "Không tìm thấy ảnh trong lịch sử." else null
                    )
                }
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Không thể tải dữ liệu."
                    )
                }
            }
        }
    }

    fun buildChatPrompt(): String {
        val info = _state.value.scan?.info ?: return ""
        if (!info.isLandmark || info.name.isBlank()) {
            return "Tôi vừa xem lại một ảnh trong lịch sử nhưng chưa rõ địa điểm. Bạn có thể gợi ý cho tôi vài địa danh nổi tiếng nên tham quan ở Việt Nam không?"
        }
        return buildString {
            append("Tôi đã nhận diện được địa điểm: ")
            append(info.name)
            if (info.location.isNotBlank()) {
                append(" (")
                append(info.location)
                append(")")
            }
            append(". Bạn hãy gợi ý cho tôi:\n")
            append("- Lịch trình tham quan 1 ngày tại đây\n")
            append("- Cách di chuyển từ Việt Nam (nếu ở nước ngoài) hoặc từ trung tâm thành phố\n")
            append("- Các địa điểm ăn uống và lưu trú gần đó\n")
            append("- Chi phí ước tính cho một du khách Việt tự túc")
        }
    }
}

data class LandmarkHistoryDetailState(
    val isLoading: Boolean = true,
    val scan: LandmarkScan? = null,
    val bitmap: Bitmap? = null,
    val errorMessage: String? = null
)
