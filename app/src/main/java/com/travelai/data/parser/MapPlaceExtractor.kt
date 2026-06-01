package com.travelai.data.parser

import com.travelai.data.model.TripMapPlaceCandidate
import com.travelai.data.model.TripPlanDay
import com.travelai.data.model.TripPlanPeriodType

object MapPlaceExtractor {
    fun extract(
        days: List<TripPlanDay>,
        destination: String
    ): List<TripMapPlaceCandidate> {
        val cleanDestination = destination.trim()
        val candidates = days.flatMap { day ->
            day.periods.flatMap { period ->
                extractFromPeriod(
                    dayNumber = day.dayNumber,
                    period = period.period,
                    content = period.content,
                    destination = cleanDestination
                )
            }
        }

        return candidates
            .distinctBy { candidate ->
                "${candidate.dayNumber}|${candidate.period.name}|${candidate.name.normalizedKey()}"
            }
    }

    private fun extractFromPeriod(
        dayNumber: Int,
        period: TripPlanPeriodType,
        content: String,
        destination: String
    ): List<TripMapPlaceCandidate> =
        content.lineSequence()
            .flatMap { line -> extractNamesFromLine(line).asSequence() }
            .map { name ->
                TripMapPlaceCandidate(
                    dayNumber = dayNumber,
                    period = period,
                    name = name,
                    query = buildQuery(name, destination)
                )
            }
            .toList()

    private fun extractNamesFromLine(rawLine: String): List<String> {
        val line = rawLine.cleanLine()
        if (line.isBlank()) return emptyList()

        val matches = placeCueRegex.findAll(line).flatMap { match ->
            match.groupValues
                .getOrNull(1)
                .orEmpty()
                .splitPlaceGroup()
                .asSequence()
        }.toList()

        return if (matches.isNotEmpty()) {
            matches
        } else {
            line.fallbackPlaceCandidates()
        }
    }

    private fun String.splitPlaceGroup(): List<String> =
        cutAfterStopPhrase()
            .split(",", ";", " và ", " & ", " + ")
            .map { it.cleanName() }
            .filter { it.isLikelyPlaceName() }

    private fun String.fallbackPlaceCandidates(): List<String> {
        val candidates = namedPlaceRegex.findAll(this).map { match ->
            match.value.cleanName()
        }
        return candidates.filter { it.isLikelyPlaceName() }.toList()
    }

    private fun String.cleanLine(): String =
        replace("**", "")
            .replace(markdownLinkRegex, "$1")
            .replace(bulletPrefixRegex, "")
            .replace(timePrefixRegex, "")
            .trim()

    private fun String.cleanName(): String =
        replace(parenthesesRegex, "")
            .replace(priceRegex, "")
            .replace(leadingNoiseRegex, "")
            .trim()
            .trim('-', ':', '.', ' ', '"', '\'', '“', '”')
            .trim()

    private fun String.cutAfterStopPhrase(): String {
        val stopIndex = stopPhraseRegex.find(this)?.range?.first ?: return this
        return take(stopIndex)
    }

    private fun String.isLikelyPlaceName(): Boolean {
        val value = trim()
        if (value.length < 3 || value.length > 80) return false
        if (value.split(Regex("\\s+")).size > 8) return false

        val lower = value.lowercase()
        if (nonPlacePrefixes.any { lower.startsWith(it) }) return false
        if (nonPlaceValues.any { lower == it }) return false

        return value.any(Char::isLetter)
    }

    private fun buildQuery(name: String, destination: String): String =
        if (destination.isBlank() || name.contains(destination, ignoreCase = true)) {
            name
        } else {
            "$name, $destination"
        }

    private fun String.normalizedKey(): String =
        lowercase()
            .replace(Regex("\\s+"), " ")
            .trim()

    private val placeCueRegex = Regex(
        pattern = """(?:di\s+chuyển\s+đến|tham\s+quan|thăm|ghé|dạo|đi|đến|tắm\s+biển|vui\s+chơi(?:\s+(?:ở|tại))?|mua\s+sắm(?:\s+(?:ở|tại))?|ăn\s+(?:sáng|trưa|tối)(?:\s+(?:ở|tại))?|uống(?:\s+cà\s+phê|\s+nước)?(?:\s+(?:ở|tại))?|check-?in(?:\s+(?:ở|tại))?|nghỉ\s+(?:ở|tại))\s+([^.;\n]+)""",
        option = RegexOption.IGNORE_CASE
    )
    private val namedPlaceRegex = Regex(
        pattern = """\b(?:bãi\s+biển|chùa|cầu|bảo\s+tàng|phố\s+cổ|bán\s+đảo|núi|đỉnh|đèo|vịnh|hồ|suối|thác|làng|chợ|công\s+viên|nhà\s+thờ|quảng\s+trường|khu\s+du\s+lịch)\s+[^,.;\n]+""",
        option = RegexOption.IGNORE_CASE
    )
    private val markdownLinkRegex = Regex("""\[([^\]]+)]\([^)]+\)""")
    private val bulletPrefixRegex = Regex("""^\s*(?:[-*+]|\d+[.)])\s*""")
    private val timePrefixRegex = Regex("""^\s*\d{1,2}[:h]\d{0,2}\s*(?:[-–—]\s*)?""")
    private val parenthesesRegex = Regex("""\([^)]*\)""")
    private val priceRegex = Regex("""\b\d+[\d.,]*\s*(?:k|đ|vnd|vnđ|triệu)\b""", RegexOption.IGNORE_CASE)
    private val leadingNoiseRegex = Regex(
        pattern = """^(?:ở|tại|khu|quanh|ven|gần|về|lại|qua)\s+""",
        option = RegexOption.IGNORE_CASE
    )
    private val stopPhraseRegex = Regex(
        pattern = """\s+(?:để|rồi|sau đó|trước khi|khoảng|trong|với|nếu|cho|kèm|và ăn|ăn)\b""",
        option = RegexOption.IGNORE_CASE
    )
    private val nonPlacePrefixes = listOf(
        "ăn ",
        "uống ",
        "món ",
        "nghỉ ngơi",
        "mua quà",
        "về khách sạn",
        "quay lại",
        "di chuyển"
    )
    private val nonPlaceValues = listOf(
        "trung tâm",
        "khách sạn",
        "nhà hàng",
        "quán ăn",
        "mì quảng",
        "cao lầu",
        "hải sản"
    )
}
