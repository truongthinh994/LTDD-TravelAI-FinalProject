package com.travelai.data.model

data class TripMapPlace(
    val id: Long,
    val sessionId: Long,
    val dayNumber: Int,
    val period: TripPlanPeriodType,
    val name: String,
    val query: String,
    val latitude: Double?,
    val longitude: Double?,
    val status: TripMapPlaceStatus,
    val createdAt: Long,
    val updatedAt: Long
) {
    val hasCoordinates: Boolean
        get() = latitude != null && longitude != null
}

enum class TripMapPlaceStatus {
    PENDING,
    RESOLVED,
    FAILED
}

data class TripMapPlaceCandidate(
    val dayNumber: Int,
    val period: TripPlanPeriodType,
    val name: String,
    val query: String
)

data class TripMapData(
    val sessionId: Long,
    val title: String,
    val destination: String,
    val places: List<TripMapPlace>
)
