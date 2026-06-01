package com.travelai.ui.landmark

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelai.data.model.LandmarkInfo
import com.travelai.data.repository.LandmarkScanRepository
import com.travelai.data.repository.VisionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class LandmarkScannerViewModel @Inject constructor(
    private val visionRepository: VisionRepository,
    private val landmarkScanRepository: LandmarkScanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LandmarkScannerUiState())
    val uiState: StateFlow<LandmarkScannerUiState> = _uiState.asStateFlow()

    fun onImageSelected(bitmap: Bitmap) {
        _uiState.update {
            it.copy(
                selectedBitmap = bitmap,
                result = null,
                errorMessage = null
            )
        }
    }

    fun onRecognize() {
        val bitmap = _uiState.value.selectedBitmap ?: return
        if (_uiState.value.isRecognizing) return

        _uiState.update {
            it.copy(isRecognizing = true, errorMessage = null, result = null)
        }

        viewModelScope.launch {
            val result = runCatching { visionRepository.recognizeLandmark(bitmap) }
            result.fold(
                onSuccess = { landmark ->
                    _uiState.update {
                        it.copy(
                            isRecognizing = false,
                            result = landmark,
                            errorMessage = null
                        )
                    }
                    if (landmark.isLandmark && landmark.name.isNotBlank()) {
                        runCatching { landmarkScanRepository.save(bitmap, landmark) }
                            .onFailure { Log.w("LandmarkScanner", "Auto-save failed", it) }
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isRecognizing = false,
                            errorMessage = throwable.toUserMessage()
                        )
                    }
                }
            )
        }
    }

    fun onClear() {
        _uiState.update { LandmarkScannerUiState() }
    }

    fun onDismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun buildChatPrompt(): String {
        val landmark = _uiState.value.result ?: return ""
        if (!landmark.isLandmark || landmark.name.isBlank()) {
            return "Tôi vừa chụp một ảnh nhưng chưa rõ địa điểm. Bạn có thể gợi ý cho tôi vài địa danh nổi tiếng nên tham quan ở Việt Nam không?"
        }
        return buildString {
            append("Tôi vừa nhận diện được địa điểm: ")
            append(landmark.name)
            if (landmark.location.isNotBlank()) {
                append(" (")
                append(landmark.location)
                append(")")
            }
            append(". Bạn hãy gợi ý cho tôi:\n")
            append("- Lịch trình tham quan 1 ngày tại đây\n")
            append("- Cách di chuyển từ Việt Nam (nếu ở nước ngoài) hoặc từ trung tâm thành phố\n")
            append("- Các địa điểm ăn uống và lưu trú gần đó\n")
            append("- Chi phí ước tính cho một du khách Việt tự túc")
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is TimeoutCancellationException,
        is SocketTimeoutException ->
            "Yêu cầu quá lâu, vui lòng thử lại (kiểm tra mạng nếu cần)."

        is IOException ->
            "Không có kết nối mạng. Hãy kiểm tra WiFi/4G rồi thử lại."

        is HttpException -> {
            val body = runCatching { response()?.errorBody()?.string().orEmpty() }
                .getOrNull()
                ?.take(400)
                ?.trim()
                .orEmpty()
            val suffix = if (body.isNotBlank()) "\n\nChi tiết: $body" else ""
            when (code()) {
                400 -> "Yêu cầu sai định dạng (400). Có thể model/baseUrl của VisionProvider không hỗ trợ vision hoặc field nào đó sai.$suffix"
                401 -> "API key bị từ chối (401). Kiểm tra GEMINI_API_KEY/OPENCODE_API_KEY trong local.properties."
                403 -> "API key không có quyền (403).$suffix"
                404 -> "Endpoint không tồn tại (404). Kiểm tra baseUrl/model trong VisionProvider."
                413 -> "Ảnh quá lớn. Vui lòng thử ảnh nhỏ hơn."
                429 -> "Đã hết quota free. Thử lại sau hoặc dùng key có hạn mức cao hơn."
                in 500..599 -> "Máy chủ AI lỗi (${code()}). Hãy thử lại sau ít phút."
                else -> "Lỗi từ máy chủ AI (${code()}).$suffix"
            }
        }

        else -> message?.takeIf { it.isNotBlank() }
            ?: "Có lỗi không xác định khi nhận diện ảnh."
    }
}

data class LandmarkScannerUiState(
    val selectedBitmap: Bitmap? = null,
    val isRecognizing: Boolean = false,
    val result: LandmarkInfo? = null,
    val errorMessage: String? = null
)
