package com.travelai.data.parser

import com.travelai.data.model.TripPlanDay
import com.travelai.data.model.TripPlanPeriod
import com.travelai.data.model.TripPlanPeriodType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapPlaceExtractorTest {
    @Test
    fun extract_readsPlacesFromStructuredItineraryPeriods() {
        val days = listOf(
            TripPlanDay(
                dayNumber = 1,
                title = "Sơn Trà và biển Mỹ Khê",
                periods = listOf(
                    TripPlanPeriod(
                        period = TripPlanPeriodType.MORNING,
                        content = """
                            - Tắm biển Bãi biển Mỹ Khê.
                            - Tham quan chùa Linh Ứng và bán đảo Sơn Trà.
                        """.trimIndent()
                    )
                )
            ),
            TripPlanDay(
                dayNumber = 2,
                title = "Hội An",
                periods = listOf(
                    TripPlanPeriod(
                        period = TripPlanPeriodType.EVENING,
                        content = "Dạo phố cổ Hội An, thả đèn hoa đăng."
                    )
                )
            )
        )

        val places = MapPlaceExtractor.extract(days, destination = "Đà Nẵng")
        val names = places.map { it.name }

        assertTrue(names.contains("Bãi biển Mỹ Khê"))
        assertTrue(names.contains("chùa Linh Ứng"))
        assertTrue(names.contains("bán đảo Sơn Trà"))
        assertTrue(names.contains("phố cổ Hội An"))
        assertTrue(places.all { it.query.isNotBlank() })
        assertTrue(places.first { it.name == "Bãi biển Mỹ Khê" }.query.contains("Đà Nẵng"))
    }

    @Test
    fun extract_dedupesPlacesWithinSameDayAndPeriod() {
        val days = listOf(
            TripPlanDay(
                dayNumber = 1,
                title = "",
                periods = listOf(
                    TripPlanPeriod(
                        period = TripPlanPeriodType.AFTERNOON,
                        content = """
                            Tham quan Chùa Cầu.
                            Ghé Chùa Cầu để chụp ảnh.
                        """.trimIndent()
                    )
                )
            )
        )

        val places = MapPlaceExtractor.extract(days, destination = "Hội An")

        assertEquals(1, places.count { it.name.equals("Chùa Cầu", ignoreCase = true) })
    }

    @Test
    fun extract_returnsEmptyWhenItineraryHasNoClearPlace() {
        val days = listOf(
            TripPlanDay(
                dayNumber = 1,
                title = "",
                periods = listOf(
                    TripPlanPeriod(
                        period = TripPlanPeriodType.MORNING,
                        content = "Nghỉ ngơi, kiểm tra thời tiết và chuẩn bị giấy tờ."
                    )
                )
            )
        )

        val places = MapPlaceExtractor.extract(days, destination = "")

        assertTrue(places.isEmpty())
    }
}
