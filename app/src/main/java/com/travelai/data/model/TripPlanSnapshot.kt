package com.travelai.data.model

data class TripPlanSnapshot(
    val sessionId: Long,
    val rawResponse: String,
    val days: List<TripPlanDay>,
    val createdAt: Long,
    val updatedAt: Long
) {
    val hasParsedItinerary: Boolean
        get() = days.isNotEmpty()
}

data class TripPlanDay(
    val dayNumber: Int,
    val title: String,
    val periods: List<TripPlanPeriod>
)

data class TripPlanPeriod(
    val period: TripPlanPeriodType,
    val content: String
)

enum class TripPlanPeriodType(val label: String) {
    MORNING("Sáng"),
    AFTERNOON("Chiều"),
    EVENING("Tối")
}
