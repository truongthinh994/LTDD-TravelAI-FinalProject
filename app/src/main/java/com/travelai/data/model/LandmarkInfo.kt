package com.travelai.data.model

data class LandmarkInfo(
    val isLandmark: Boolean,
    val name: String,
    val location: String,
    val description: String,
    val history: String,
    val tips: List<String>,
    val confidence: Float
) {
    companion object {
        val EMPTY = LandmarkInfo(
            isLandmark = false,
            name = "",
            location = "",
            description = "",
            history = "",
            tips = emptyList(),
            confidence = 0f
        )
    }
}
