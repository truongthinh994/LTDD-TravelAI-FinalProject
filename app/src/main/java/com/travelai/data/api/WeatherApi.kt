package com.travelai.data.api

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open-Meteo daily forecast API. No API key required.
 *
 * Docs: https://open-meteo.com/en/docs
 */
interface WeatherApi {
    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: String = DEFAULT_DAILY_FIELDS,
        @Query("timezone") timezone: String = "Asia/Bangkok",
        @Query("forecast_days") forecastDays: Int = 7
    ): WeatherForecastResponse

    @GET("https://geocoding-api.open-meteo.com/v1/search")
    suspend fun searchLocation(
        @Query("name") name: String,
        @Query("count") count: Int = 5,
        @Query("language") language: String = "vi",
        @Query("format") format: String = "json"
    ): WeatherGeocodingResponse

    companion object {
        const val DEFAULT_DAILY_FIELDS: String =
            "temperature_2m_max,temperature_2m_min,weathercode,precipitation_probability_max"
    }
}
