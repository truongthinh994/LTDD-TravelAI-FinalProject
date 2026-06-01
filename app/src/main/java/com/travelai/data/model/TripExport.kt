package com.travelai.data.model

data class TripExport(
    val title: String,
    val tripProfile: TripProfile?,
    val tripPlanSnapshot: TripPlanSnapshot?,
    val budgetItems: List<BudgetItem>,
    val checklistItems: List<ChecklistItem>,
    val fallbackAssistantText: String?
)

fun TripExport.toShareText(): String = buildList {
    add(title.trim().ifBlank { DEFAULT_EXPORT_TITLE })
    addTripProfile(tripProfile)
    addItinerary(tripPlanSnapshot, fallbackAssistantText)
    addBudget(budgetItems)
    addChecklist(checklistItems)
}.joinToString(separator = "\n").trim()

private fun MutableList<String>.addTripProfile(profile: TripProfile?) {
    if (profile == null) return

    add("")
    add("Thông tin chuyến đi")
    add("- Điểm đến: ${profile.destination.trim()}")
    add("- Số ngày: ${profile.days}")
    add("- Số người: ${profile.people}")
    addOptionalLine("Ngân sách", profile.budget)
    addOptionalLine("Phong cách", profile.travelStyle)
    addOptionalLine("Phương tiện", profile.transport)
    addOptionalLine("Ghi chú", profile.note)
}

private fun MutableList<String>.addItinerary(
    snapshot: TripPlanSnapshot?,
    fallbackAssistantText: String?
) {
    val parsedDays = snapshot?.days.orEmpty()
    val rawText = snapshot?.rawResponse?.trim().orEmpty()
    val fallbackText = fallbackAssistantText?.trim().orEmpty()

    when {
        parsedDays.isNotEmpty() -> {
            add("")
            add("Lịch trình")
            parsedDays.sortedBy { it.dayNumber }.forEach { day ->
                add("")
                add(day.exportTitle())
                day.periods.forEach { period ->
                    val content = period.content.trim()
                    if (content.isNotBlank()) {
                        add("- ${period.period.label}: ${content.indentContinuation()}")
                    }
                }
            }
        }

        rawText.isNotBlank() -> {
            add("")
            add("Lịch trình")
            add(rawText)
        }

        fallbackText.isNotBlank() -> {
            add("")
            add("Gợi ý từ TravelAI")
            add(fallbackText)
        }
    }
}

private fun MutableList<String>.addBudget(items: List<BudgetItem>) {
    if (items.isEmpty()) return

    add("")
    add("Ngân sách dự kiến")
    items.forEach { item ->
        val note = item.note.trim().takeIf { it.isNotBlank() }?.let { " - $it" }.orEmpty()
        add("- ${item.category.label}: ${item.title} - ${formatBudgetAmount(item.amountVnd)}$note")
    }
    add("Tổng: ${formatBudgetAmount(items.totalAmountVnd())}")
}

private fun MutableList<String>.addChecklist(items: List<ChecklistItem>) {
    if (items.isEmpty()) return

    add("")
    add("Checklist chuẩn bị")
    items.forEach { item ->
        val mark = if (item.isChecked) "[x]" else "[ ]"
        add("- $mark ${item.title}")
    }
}

private fun MutableList<String>.addOptionalLine(label: String, value: String) {
    val cleanValue = value.trim()
    if (cleanValue.isNotBlank()) {
        add("- $label: $cleanValue")
    }
}

private fun TripPlanDay.exportTitle(): String {
    val cleanTitle = title.trim()
    return if (cleanTitle.isBlank()) {
        "Ngày $dayNumber"
    } else {
        "Ngày $dayNumber: $cleanTitle"
    }
}

private fun String.indentContinuation(): String =
    lineSequence()
        .joinToString(separator = "\n  ") { it.trim() }

private const val DEFAULT_EXPORT_TITLE = "Lịch trình TravelAI"
