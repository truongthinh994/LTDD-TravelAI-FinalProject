package com.travelai.ui.landmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelai.data.model.LandmarkScan
import com.travelai.data.repository.LandmarkScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LandmarkHistoryViewModel @Inject constructor(
    private val repository: LandmarkScanRepository
) : ViewModel() {

    val items: StateFlow<List<LandmarkScan>> = repository.observeHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = emptyList()
        )

    fun delete(id: Long) {
        viewModelScope.launch {
            runCatching { repository.delete(id) }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
