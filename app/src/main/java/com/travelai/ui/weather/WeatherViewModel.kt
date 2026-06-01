package com.travelai.ui.weather

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelai.data.model.WeatherDay
import com.travelai.data.repository.WeatherRepository
import com.travelai.ui.navigation.TravelAiRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

@HiltViewModel
class WeatherViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val weatherRepository: WeatherRepository
) : ViewModel() {
    private val initialDestination: String =
        savedStateHandle.get<String>(TravelAiRoutes.DESTINATION_ARG).orEmpty()

    private val _uiState = MutableStateFlow(
        WeatherUiState(query = initialDestination)
    )
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        if (initialDestination.isNotBlank()) {
            loadForecast()
        }
    }

    fun onQueryChange(value: String) {
        _uiState.update {
            it.copy(
                query = value,
                errorMessage = null
            )
        }
    }

    fun loadForecast() {
        val destination = _uiState.value.query.trim()
        if (destination.isBlank()) {
            _uiState.update {
                it.copy(
                    days = emptyList(),
                    packingAdvice = emptyList(),
                    loadedDestination = "",
                    errorMessage = "Nhập điểm đến để xem dự báo thời tiết."
                )
            }
            return
        }
        if (_uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val forecast = weatherRepository.getForecastBundle(destination)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        days = forecast?.days.orEmpty(),
                        packingAdvice = forecast?.packingAdvice.orEmpty(),
                        loadedDestination = forecast?.destination.orEmpty(),
                        errorMessage = if (forecast == null) {
                            "Chưa có dữ liệu thời tiết cho điểm đến này. Thử tên thành phố phổ biến như Đà Nẵng, Hà Nội, Đà Lạt."
                        } else {
                            null
                        }
                    )
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.toUserMessage()
                    )
                }
            }
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is IOException -> "Không có kết nối mạng. Hãy kiểm tra WiFi/4G rồi thử lại."
        is HttpException -> "Dịch vụ thời tiết lỗi (${code()}). Hãy thử lại sau ít phút."
        else -> message?.takeIf { it.isNotBlank() }
            ?: "Không thể tải dự báo thời tiết lúc này."
    }
}

data class WeatherUiState(
    val query: String = "",
    val days: List<WeatherDay> = emptyList(),
    val packingAdvice: List<String> = emptyList(),
    val loadedDestination: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
