package com.travelai.data.api

import com.google.gson.annotations.SerializedName

data class WeatherForecastResponse(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timezone: String? = null,
    val daily: WeatherDailyBlock? = null
)

data class WeatherDailyBlock(
    val time: List<String> = emptyList(),
    @SerializedName("temperature_2m_max")
    val temperatureMax: List<Double> = emptyList(),
    @SerializedName("temperature_2m_min")
    val temperatureMin: List<Double> = emptyList(),
    @SerializedName("weathercode")
    val weatherCode: List<Int> = emptyList(),
    @SerializedName("precipitation_probability_max")
    val precipitationProbabilityMax: List<Int> = emptyList()
)

data class WeatherGeocodingResponse(
    val results: List<WeatherGeocodingResult> = emptyList()
)

data class WeatherGeocodingResult(
    val id: Long? = null,
    val name: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val country: String? = null,
    @SerializedName("admin1")
    val admin1: String? = null
) {
    val displayName: String
        get() = listOf(name, admin1, country)
            .filterNot { it.isNullOrBlank() }
            .joinToString(", ")
}
