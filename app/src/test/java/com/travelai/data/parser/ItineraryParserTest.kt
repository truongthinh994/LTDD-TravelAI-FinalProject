package com.travelai.data.parser

import com.travelai.data.model.TripPlanPeriodType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ItineraryParserTest {
    @Test
    fun parseDays_extractsThreeDayDaNangItineraryByPeriods() {
        val rawResponse = """
            ## Ngày 1: Bãi biển Mỹ Khê và Sơn Trà
            **Sáng:** Tắm biển Mỹ Khê, ăn sáng mì Quảng.
            **Chiều:** Tham quan chùa Linh Ứng và bán đảo Sơn Trà.
            **Tối:** Dạo cầu Rồng, ăn hải sản ven biển.

            ## Ngày 2: Hội An
            **Sáng:** Di chuyển đến phố cổ Hội An.
            **Chiều:** Thăm nhà cổ, chùa Cầu và uống nước Mót.
            **Tối:** Thả đèn hoa đăng, ăn cao lầu.

            ## Ngày 3: Bà Nà Hills
            **Sáng:** Đi Bà Nà Hills và Cầu Vàng.
            **Chiều:** Vui chơi Fantasy Park.
            **Tối:** Quay lại Đà Nẵng, mua quà.
        """.trimIndent()

        val days = ItineraryParser.parseDays(rawResponse)

        assertEquals(3, days.size)
        assertEquals(1, days[0].dayNumber)
        assertEquals("Bãi biển Mỹ Khê và Sơn Trà", days[0].title)
        assertEquals(TripPlanPeriodType.MORNING, days[0].periods[0].period)
        assertTrue(days[0].periods[0].content.contains("Mỹ Khê"))
        assertEquals(TripPlanPeriodType.AFTERNOON, days[1].periods[1].period)
        assertTrue(days[2].periods[2].content.contains("mua quà"))
    }

    @Test
    fun parseDays_supportsPlainPeriodHeadersWithBodyLines() {
        val rawResponse = """
            Ngày 1
            Sáng:
            - Ăn sáng ở trung tâm.
            - Ghé bảo tàng.
            Chiều:
            Đi biển.
            Tối:
            Nghỉ ngơi.
        """.trimIndent()

        val days = ItineraryParser.parseDays(rawResponse)

        assertEquals(1, days.size)
        assertEquals(3, days[0].periods.size)
        assertTrue(days[0].periods[0].content.contains("bảo tàng"))
    }

    @Test
    fun buildSnapshot_keepsRawTextWhenParserCannotRecognizeItinerary() {
        val rawResponse = "Bạn nên mang giấy tờ tùy thân và kiểm tra thời tiết trước khi đi."

        val snapshot = ItineraryParser.buildSnapshot(
            sessionId = 42L,
            rawResponse = rawResponse,
            now = 1000L
        )

        assertEquals(42L, snapshot.sessionId)
        assertEquals(rawResponse, snapshot.rawResponse)
        assertFalse(snapshot.hasParsedItinerary)
    }
}
