package com.travelai.data.model

fun buildSmartChecklistPrompt(
    profile: TripProfile?,
    days: List<TripPlanDay>,
    weatherDays: List<WeatherDay>
): String = buildString {
    appendLine("Hãy tạo checklist chuẩn bị du lịch cho chuyến đi sau.")
    appendLine("Chỉ trả về danh sách 8-12 mục, mỗi dòng là một việc/đồ cần chuẩn bị.")
    appendLine("Không giải thích dài, không dùng markdown phức tạp.")
    appendLine()
    if (profile != null) {
        appendLine("Thông tin chuyến đi:")
        appendLine("- Điểm đến: ${profile.destination.ifBlank { "Chưa rõ" }}")
        appendLine("- Số ngày: ${profile.days}")
        appendLine("- Số người: ${profile.people}")
        appendOptionalLine("Ngân sách", profile.budget)
        appendOptionalLine("Phong cách", profile.travelStyle)
        appendOptionalLine("Phương tiện", profile.transport)
        appendOptionalLine("Ghi chú", profile.note)
    }
    if (days.isNotEmpty()) {
        appendLine()
        appendLine("Lịch trình tóm tắt:")
        days.take(7).forEach { day ->
            appendLine("- ${day.title.ifBlank { "Ngày ${day.dayNumber}" }}")
            day.periods.forEach { period ->
                appendLine("  ${period.period.label}: ${period.content.take(180)}")
            }
        }
    }
    if (weatherDays.isNotEmpty()) {
        appendLine()
        appendLine("Thời tiết dự kiến:")
        weatherDays.take(7).forEach { day ->
            appendLine("- ${day.date}: ${day.conditionLabel}, ${day.tempMinC}-${day.tempMaxC}°C, mưa ${day.rainChancePct}%")
        }
    }
    appendLine()
    append("Checklist nên thực tế cho du khách Việt, có cả giấy tờ, đồ cá nhân, tiền, sức khỏe và đồ theo thời tiết.")
}

fun parseSmartChecklistResponse(raw: String): List<String> =
    raw.lineSequence()
        .map { line ->
            line.trim()
                .removePrefix("-")
                .removePrefix("*")
                .removePrefix("•")
                .replace(Regex("""^\d+[\).]\s*"""), "")
                .replace(Regex("""^\[[ xX]\]\s*"""), "")
                .trim()
        }
        .filter { it.length >= 3 }
        .distinct()
        .take(12)
        .toList()

private fun StringBuilder.appendOptionalLine(label: String, value: String) {
    val clean = value.trim()
    if (clean.isNotBlank()) {
        appendLine("- $label: $clean")
    }
}
