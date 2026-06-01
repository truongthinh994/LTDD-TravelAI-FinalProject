package com.travelai.ui.map

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelai.data.model.TripMapPlace
import com.travelai.data.model.TripMapPlaceStatus
import com.travelai.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class TripMapViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    @ApplicationContext private val context: Context,
    @Named("MapsApiKey") mapsApiKey: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        TripMapUiState(
            isLoading = true,
            mapsApiKeyMissing = mapsApiKey.isBlank()
        )
    )
    val uiState: StateFlow<TripMapUiState> = _uiState.asStateFlow()

    private val requestedSessionId: Long? =
        savedStateHandle.get<Long>(SESSION_ID_ARG)
            ?: savedStateHandle.get<String>(SESSION_ID_ARG)?.toLongOrNull()

    private val geocoder: Geocoder by lazy {
        Geocoder(context, Locale("vi", "VN"))
    }

    init {
        loadMap()
    }

    fun loadMap() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    emptyMessage = null
                )
            }

            runCatching {
                if (requestedSessionId != null && requestedSessionId > 0L) {
                    chatRepository.loadTripMapData(requestedSessionId)
                } else {
                    chatRepository.loadLatestTripMapData()
                }
            }.onSuccess { mapData ->
                if (mapData == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            emptyMessage = "Tạo lịch trình trước để xem bản đồ chuyến đi."
                        )
                    }
                    return@onSuccess
                }

                _uiState.update {
                    it.copy(
                        sessionId = mapData.sessionId,
                        title = mapData.title,
                        destination = mapData.destination,
                        places = mapData.places,
                        selectedDayNumber = null,
                        isLoading = false,
                        emptyMessage = if (mapData.places.isEmpty()) {
                            "Chưa tìm thấy địa điểm rõ ràng trong lịch trình này."
                        } else {
                            null
                        },
                        errorMessage = null
                    )
                }
                resolvePendingPlaces(mapData.sessionId, mapData.places)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Không thể tải bản đồ chuyến đi."
                    )
                }
            }
        }
    }

    fun selectDay(dayNumber: Int?) {
        _uiState.update { it.copy(selectedDayNumber = dayNumber) }
    }

    private fun resolvePendingPlaces(
        sessionId: Long,
        places: List<TripMapPlace>
    ) {
        val pendingPlaces = places.filter { it.status == TripMapPlaceStatus.PENDING }
        if (pendingPlaces.isEmpty()) return

        viewModelScope.launch {
            if (!Geocoder.isPresent()) {
                pendingPlaces.forEach { place ->
                    chatRepository.updateTripMapPlaceGeocode(
                        sessionId = sessionId,
                        placeId = place.id,
                        latitude = null,
                        longitude = null,
                        status = TripMapPlaceStatus.FAILED
                    )
                }
                val refreshedPlaces = chatRepository.getTripMapPlaces(sessionId)
                _uiState.update {
                    it.copy(
                        places = refreshedPlaces,
                        isResolving = false,
                        geocodeMessage = "Thiết bị này không hỗ trợ Geocoder, vẫn có thể mở tìm kiếm Google Maps ngoài app."
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isResolving = true, geocodeMessage = null) }

            pendingPlaces.forEach { place ->
                val coordinate = withTimeoutOrNull(GEOCODE_TIMEOUT_MS) {
                    geocode(place.query)
                }
                chatRepository.updateTripMapPlaceGeocode(
                    sessionId = sessionId,
                    placeId = place.id,
                    latitude = coordinate?.latitude,
                    longitude = coordinate?.longitude,
                    status = if (coordinate != null) {
                        TripMapPlaceStatus.RESOLVED
                    } else {
                        TripMapPlaceStatus.FAILED
                    }
                )
                val refreshedPlaces = chatRepository.getTripMapPlaces(sessionId)
                _uiState.update {
                    it.copy(places = refreshedPlaces)
                }
            }

            _uiState.update { it.copy(isResolving = false) }
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun geocode(query: String): GeocodedCoordinate? =
        withContext(Dispatchers.IO) {
            runCatching {
                geocoder.getFromLocationName(query, 1)
                    ?.firstOrNull()
                    ?.let { address ->
                        GeocodedCoordinate(
                            latitude = address.latitude,
                            longitude = address.longitude
                        )
                    }
            }.getOrNull()
        }

    private companion object {
        const val SESSION_ID_ARG = "sessionId"
        const val GEOCODE_TIMEOUT_MS = 8_000L
    }
}

data class TripMapUiState(
    val sessionId: Long? = null,
    val title: String = "",
    val destination: String = "",
    val places: List<TripMapPlace> = emptyList(),
    val selectedDayNumber: Int? = null,
    val isLoading: Boolean = false,
    val isResolving: Boolean = false,
    val mapsApiKeyMissing: Boolean = false,
    val errorMessage: String? = null,
    val emptyMessage: String? = null,
    val geocodeMessage: String? = null
) {
    val dayNumbers: List<Int>
        get() = places.map { it.dayNumber }.distinct().sorted()

    val visiblePlaces: List<TripMapPlace>
        get() = selectedDayNumber?.let { selectedDay ->
            places.filter { it.dayNumber == selectedDay }
        } ?: places

    val visibleResolvedPlaces: List<TripMapPlace>
        get() = visiblePlaces.filter { it.hasCoordinates }
}

private data class GeocodedCoordinate(
    val latitude: Double,
    val longitude: Double
)
