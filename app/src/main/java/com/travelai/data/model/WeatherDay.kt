package com.travelai.data.model

/**
 * One day's forecast surfaced to the UI. All values are already normalized
 * (rounded temperatures, emoji picked from the WMO weather code).
 */
data class WeatherDay(
    val date: String,
    val tempMinC: Int,
    val tempMaxC: Int,
    val conditionEmoji: String,
    val conditionLabel: String,
    val rainChancePct: Int
)

data class WeatherForecast(
    val destination: String,
    val days: List<WeatherDay>,
    val packingAdvice: List<String>
)

fun List<WeatherDay>.toPackingAdvice(): List<String> {
    if (isEmpty()) return emptyList()

    val maxTemp = maxOf { it.tempMaxC }
    val minTemp = minOf { it.tempMinC }
    val maxRain = maxOf { it.rainChancePct }
    val labels = map { it.conditionLabel.lowercase() }
    val advice = mutableListOf<String>()

    if (maxRain >= 50 || labels.any { it.contains("mưa") || it.contains("dông") }) {
        advice += "Mang áo mưa gọn nhẹ hoặc ô nhỏ."
        advice += "Ưu tiên giày dép chống trượt cho ngày có mưa."
    }
    if (maxTemp >= 32 || labels.any { it.contains("nắng") }) {
        advice += "Chuẩn bị kem chống nắng, mũ và nước uống."
    }
    if (minTemp <= 18) {
        advice += "Mang áo khoác mỏng cho sáng sớm hoặc buổi tối."
    }
    if (advice.isEmpty()) {
        advice += "Thời tiết khá dễ chịu, chuẩn bị đồ cơ bản và kiểm tra lại trước ngày đi."
    }
    return advice.distinct()
}
