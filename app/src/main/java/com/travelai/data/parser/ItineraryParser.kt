package com.travelai.data.parser

import com.travelai.data.model.TripPlanDay
import com.travelai.data.model.TripPlanPeriod
import com.travelai.data.model.TripPlanPeriodType
import com.travelai.data.model.TripPlanSnapshot

object ItineraryParser {
    fun buildSnapshot(
        sessionId: Long,
        rawResponse: String,
        now: Long
    ): TripPlanSnapshot = TripPlanSnapshot(
        sessionId = sessionId,
        rawResponse = rawResponse,
        days = parseDays(rawResponse),
        createdAt = now,
        updatedAt = now
    )

    fun parseDays(rawResponse: String): List<TripPlanDay> {
        val days = mutableListOf<MutableTripPlanDay>()
        var currentDay: MutableTripPlanDay? = null
        var currentPeriod: MutableTripPlanPeriod? = null

        fun flushPeriod() {
            val period = currentPeriod ?: return
            val content = period.lines.joinToString("\n").trim()
            if (content.isNotBlank()) {
                currentDay?.periods?.add(
                    TripPlanPeriod(
                        period = period.type,
                        content = content
                    )
                )
            }
            currentPeriod = null
        }

        fun flushDay() {
            flushPeriod()
            val day = currentDay ?: return
            if (day.periods.isNotEmpty()) {
                days += day
            }
            currentDay = null
        }

        rawResponse.lineSequence().forEach { rawLine ->
            // Strip emoji and other decorative Unicode symbols *before* regex
            // matching so headers like "### 🗓️ **Ngày 1**" still parse as a
            // day header. We keep punctuation, digits, and letters intact.
            val line = rawLine.stripDecorativeSymbols().trim()
            if (line.isBlank()) return@forEach

            val dayMatch = dayHeaderRegex.matchEntire(line)
            if (dayMatch != null) {
                flushDay()
                currentDay = MutableTripPlanDay(
                    dayNumber = dayMatch.groupValues[1].toInt(),
                    title = cleanHeaderTail(dayMatch.groupValues.getOrElse(2) { "" })
                )
                return@forEach
            }

            val periodMatch = periodHeaderRegex.matchEntire(line)
            if (periodMatch != null && currentDay != null) {
                flushPeriod()
                currentPeriod = MutableTripPlanPeriod(
                    type = periodMatch.groupValues[1].toPeriodType(),
                    lines = mutableListOf<String>().apply {
                        val inlineContent = cleanHeaderTail(
                            periodMatch.groupValues.getOrElse(2) { "" }
                        )
                        if (inlineContent.isNotBlank()) {
                            add(inlineContent)
                        }
                    }
                )
                return@forEach
            }

            currentPeriod?.lines?.add(cleanContentLine(line))
        }

        flushDay()

        return days.map { day ->
            TripPlanDay(
                dayNumber = day.dayNumber,
                title = day.title,
                periods = day.periods
            )
        }
    }

    private fun String.toPeriodType(): TripPlanPeriodType = when (lowercase()) {
        "sáng" -> TripPlanPeriodType.MORNING
        "trưa", "chiều" -> TripPlanPeriodType.AFTERNOON
        else -> TripPlanPeriodType.EVENING // "tối", "đêm"
    }

    private fun cleanHeaderTail(value: String): String =
        value
            .replace("**", "")
            .trim()
            .trim(':', '-', '–', '—', '.', ' ')
            .trim()

    private fun cleanContentLine(value: String): String =
        value
            .replace("**", "")
            .trim()

    private data class MutableTripPlanDay(
        val dayNumber: Int,
        val title: String,
        val periods: MutableList<TripPlanPeriod> = mutableListOf()
    )

    private data class MutableTripPlanPeriod(
        val type: TripPlanPeriodType,
        val lines: MutableList<String>
    )

    /**
     * Drop Unicode "Other_Symbol" (emoji like 🗓️📅⛅), surrogate halves of
     * astral-plane emoji, variation selectors, and the zero-width joiner.
     * Collapses any whitespace gap created by removal so the resulting string
     * is regex-friendly.
     */
    private fun String.stripDecorativeSymbols(): String =
        replace(Regex("[\\p{So}\\p{Cs}\\uFE00-\\uFE0F\\u200D]"), "")
            .replace(Regex("\\s+"), " ")

    private val dayHeaderRegex = Regex(
        pattern = """^\s*(?:#{1,6}\s*)?(?:\d+[.)]\s*)?(?:[-*+]\s*)?(?:\*\*)?Ngày\s+(\d+)\s*(?:[:.\-–—]\s*)?(.*?)(?:\*\*)?\s*$""",
        option = RegexOption.IGNORE_CASE
    )

    private val periodHeaderRegex = Regex(
        pattern = """^\s*(?:#{1,6}\s*)?(?:\d+[.)]\s*)?(?:[-*+]\s*)?(?:\*\*)?(?:Buổi\s+)?(Sáng|Chiều|Tối|Trưa|Đêm)\s*(?:[:.\-–—]\s*)?(.*?)(?:\*\*)?\s*$""",
        option = RegexOption.IGNORE_CASE
    )
}
