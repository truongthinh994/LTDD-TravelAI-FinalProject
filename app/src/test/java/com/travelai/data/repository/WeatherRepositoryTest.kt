package com.travelai.data.repository

import com.travelai.data.api.WeatherApi
import com.travelai.data.api.WeatherDailyBlock
import com.travelai.data.api.WeatherForecastResponse
import com.travelai.data.api.WeatherGeocodingResponse
import com.travelai.data.api.WeatherGeocodingResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherRepositoryTest {
    @Test
    fun getForecast_usesOpenMeteoGeocodingAndNormalizesResponse() = runBlocking {
        val api = FakeWeatherApi(
            geocodingResults = listOf(
                WeatherGeocodingResult(
                    name = "Đà Nẵng",
                    latitude = 16.0678,
                    longitude = 108.2208,
                    country = "Vietnam"
                )
            )
        )
        val repository = WeatherRepository(api)

        val forecast = repository.getForecastBundle("Du lịch Đà Nẵng")

        assertEquals(1, api.searchCount)
        assertEquals(1, api.forecastCount)
        assertEquals(16.0678, api.lastLatitude!!, 0.0001)
        assertEquals(108.2208, api.lastLongitude!!, 0.0001)
        assertEquals("Đà Nẵng, Vietnam", forecast?.destination)
        assertEquals(2, forecast?.days?.size)
        assertEquals("2026-05-21", forecast?.days?.first()?.date)
        assertEquals(25, forecast?.days?.first()?.tempMinC)
        assertEquals(32, forecast?.days?.first()?.tempMaxC)
        assertEquals("Nắng", forecast?.days?.first()?.conditionLabel)
        assertEquals(10, forecast?.days?.first()?.rainChancePct)
        assertTrue(forecast?.packingAdvice.orEmpty().isNotEmpty())
    }

    @Test
    fun getForecast_fallsBackToCuratedVietnamDestinations() = runBlocking {
        val api = FakeWeatherApi()
        val repository = WeatherRepository(api)

        val days = repository.getForecast("Sài Gòn")

        assertEquals(1, api.searchCount)
        assertEquals(1, api.forecastCount)
        assertEquals(10.8231, api.lastLatitude!!, 0.0001)
        assertEquals(106.6297, api.lastLongitude!!, 0.0001)
        assertEquals(2, days?.size)
    }

    @Test
    fun getForecast_returnsNullForUnsupportedDestinationWithoutForecastCall() = runBlocking {
        val api = FakeWeatherApi()
        val repository = WeatherRepository(api)

        val days = repository.getForecast("Atlantis")

        assertNull(days)
        assertEquals(1, api.searchCount)
        assertEquals(0, api.forecastCount)
    }

    @Test
    fun getForecast_reusesInMemoryCacheForSameCoordinates() = runBlocking {
        val api = FakeWeatherApi()
        val repository = WeatherRepository(api)

        val first = repository.getForecast("Sài Gòn")
        val second = repository.getForecast("Hồ Chí Minh")

        assertEquals(first, second)
        assertEquals(2, api.searchCount)
        assertEquals(1, api.forecastCount)
    }

    private class FakeWeatherApi(
        private val geocodingResults: List<WeatherGeocodingResult> = emptyList()
    ) : WeatherApi {
        var searchCount: Int = 0
        var forecastCount: Int = 0
        var lastLatitude: Double? = null
        var lastLongitude: Double? = null

        override suspend fun searchLocation(
            name: String,
            count: Int,
            language: String,
            format: String
        ): WeatherGeocodingResponse {
            searchCount += 1
            return WeatherGeocodingResponse(results = geocodingResults)
        }

        override suspend fun forecast(
            latitude: Double,
            longitude: Double,
            daily: String,
            timezone: String,
            forecastDays: Int
        ): WeatherForecastResponse {
            forecastCount += 1
            lastLatitude = latitude
            lastLongitude = longitude
            return WeatherForecastResponse(
                latitude = latitude,
                longitude = longitude,
                timezone = timezone,
                daily = WeatherDailyBlock(
                    time = listOf("2026-05-21", "2026-05-22"),
                    temperatureMax = listOf(32.6, 29.2),
                    temperatureMin = listOf(25.1, 24.8),
                    weatherCode = listOf(0, 61),
                    precipitationProbabilityMax = listOf(10, 70)
                )
            )
        }
    }
}
