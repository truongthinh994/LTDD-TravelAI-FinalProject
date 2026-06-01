package com.travelai.data.repository

import com.travelai.data.api.WeatherApi
import com.travelai.data.api.WeatherForecastResponse
import com.travelai.data.api.WeatherGeocodingResult
import com.travelai.data.model.WeatherDay
import com.travelai.data.model.WeatherForecast
import com.travelai.data.model.toPackingAdvice
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApi: WeatherApi
) {
    private val cache = mutableMapOf<String, CachedForecast>()
    private val cacheMutex = Mutex()

    suspend fun getForecast(destination: String): List<WeatherDay>? =
        getForecastBundle(destination)?.days

    suspend fun getForecastBundle(destination: String): WeatherForecast? {
        val location = geocode(destination) ?: return null
        val cacheKey = "${location.latitude},${location.longitude}"

        val now = System.currentTimeMillis()
        cacheMutex.withLock {
            val hit = cache[cacheKey]
            if (hit != null && now - hit.fetchedAt < CACHE_TTL_MS) {
                return WeatherForecast(
                    destination = location.displayName,
                    days = hit.days,
                    packingAdvice = hit.days.toPackingAdvice()
                )
            }
        }

        val response = runCatching {
            weatherApi.forecast(
                latitude = location.latitude,
                longitude = location.longitude
            )
        }.getOrElse { return null }

        val days = response.toWeatherDays()
        if (days.isEmpty()) return null

        cacheMutex.withLock {
            cache[cacheKey] = CachedForecast(
                fetchedAt = now,
                days = days
            )
        }

        return WeatherForecast(
            destination = location.displayName,
            days = days,
            packingAdvice = days.toPackingAdvice()
        )
    }

    private suspend fun geocode(destination: String): Location? {
        val rawQuery = destination.trim()
        val key = rawQuery.normalizeForGeocode()
        if (key.isBlank()) return null

        val remoteLocation = runCatching {
            weatherApi.searchLocation(rawQuery)
                .results
                .asSequence()
                .filter { it.latitude != null && it.longitude != null }
                .sortedWith(compareByDescending<WeatherGeocodingResult> {
                    it.country.equals("Vietnam", ignoreCase = true) ||
                        it.country.equals("Việt Nam", ignoreCase = true)
                }.thenByDescending {
                    rawQuery.normalizeForGeocode().contains(it.name.normalizeForGeocode())
                })
                .firstOrNull()
        }.getOrNull()

        if (remoteLocation?.latitude != null && remoteLocation.longitude != null) {
            return Location(
                latitude = remoteLocation.latitude,
                longitude = remoteLocation.longitude,
                displayName = remoteLocation.displayName.ifBlank { rawQuery }
            )
        }

        return VN_DESTINATIONS
            .filter { (alias, _) -> key.contains(alias) }
            .maxByOrNull { it.key.length }
            ?.value
    }

    private fun WeatherForecastResponse.toWeatherDays(): List<WeatherDay> {
        val daily = daily ?: return emptyList()
        val size = minOf(
            daily.time.size,
            daily.temperatureMax.size,
            daily.temperatureMin.size,
            daily.weatherCode.size
        )
        return (0 until size).map { idx ->
            val condition = mapWeatherCode(daily.weatherCode[idx])
            WeatherDay(
                date = daily.time[idx],
                tempMinC = daily.temperatureMin[idx].toInt(),
                tempMaxC = daily.temperatureMax[idx].toInt(),
                conditionEmoji = condition.first,
                conditionLabel = condition.second,
                rainChancePct = daily.precipitationProbabilityMax.getOrNull(idx) ?: 0
            )
        }
    }

    private data class Location(
        val latitude: Double,
        val longitude: Double,
        val displayName: String
    )

    private data class CachedForecast(
        val fetchedAt: Long,
        val days: List<WeatherDay>
    )

    private companion object {
        const val CACHE_TTL_MS: Long = 60L * 60L * 1000L

        val VN_DESTINATIONS: Map<String, Location> = mapOf(
            "da nang" to Location(16.0479, 108.2208, "Đà Nẵng"),
            "ha noi" to Location(21.0285, 105.8542, "Hà Nội"),
            "phu quoc" to Location(10.2899, 103.9840, "Phú Quốc"),
            "ha long" to Location(20.9520, 107.0828, "Hạ Long"),
            "sapa" to Location(22.3360, 103.8438, "Sa Pa"),
            "sa pa" to Location(22.3360, 103.8438, "Sa Pa"),
            "da lat" to Location(11.9404, 108.4583, "Đà Lạt"),
            "nha trang" to Location(12.2388, 109.1967, "Nha Trang"),
            "hoi an" to Location(15.8801, 108.3380, "Hội An"),
            "hue" to Location(16.4637, 107.5909, "Huế"),
            "phong nha" to Location(17.5346, 106.2860, "Phong Nha"),
            "mui ne" to Location(10.9333, 108.2833, "Mũi Né"),
            "phan thiet" to Location(10.9333, 108.1000, "Phan Thiết"),
            "can tho" to Location(10.0452, 105.7469, "Cần Thơ"),
            "ho chi minh" to Location(10.8231, 106.6297, "TP. Hồ Chí Minh"),
            "sai gon" to Location(10.8231, 106.6297, "TP. Hồ Chí Minh"),
            "con dao" to Location(8.6833, 106.6000, "Côn Đảo"),
            "quy nhon" to Location(13.7765, 109.2233, "Quy Nhơn"),
            "vung tau" to Location(10.3460, 107.0843, "Vũng Tàu"),
            "ninh binh" to Location(20.2506, 105.9745, "Ninh Bình")
        )

        fun mapWeatherCode(code: Int): Pair<String, String> = when (code) {
            0 -> "☀️" to "Nắng"
            1, 2 -> "🌤" to "Có nắng"
            3 -> "⛅" to "Nhiều mây"
            45, 48 -> "🌫" to "Sương mù"
            in 51..57 -> "🌦" to "Mưa phùn"
            in 61..65 -> "🌧" to "Mưa"
            in 66..67 -> "🌧" to "Mưa lạnh"
            in 71..77 -> "❄️" to "Tuyết"
            in 80..82 -> "🌧" to "Mưa rào"
            in 85..86 -> "🌨" to "Tuyết rào"
            in 95..99 -> "⛈" to "Dông"
            else -> "⛅" to "Trời mây"
        }

        fun String.normalizeForGeocode(): String =
            lowercase()
                .replace("đ", "d")
                .replace(Regex("[áàảãạăắằẳẵặâấầẩẫậ]"), "a")
                .replace(Regex("[éèẻẽẹêếềểễệ]"), "e")
                .replace(Regex("[íìỉĩị]"), "i")
                .replace(Regex("[óòỏõọôốồổỗộơớờởỡợ]"), "o")
                .replace(Regex("[úùủũụưứừửữự]"), "u")
                .replace(Regex("[ýỳỷỹỵ]"), "y")
                .replace(Regex("\\s+"), " ")
                .trim()
    }
}
