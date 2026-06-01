package com.travelai.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartChecklistTest {
    @Test
    fun parseSmartChecklistResponse_cleansBulletsNumbersAndCheckboxes() {
        val items = parseSmartChecklistResponse(
            """
            1. Mang CCCD
            - Sạc dự phòng
            * Kem chống nắng
            [ ] Áo mưa gọn nhẹ
            """.trimIndent()
        )

        assertEquals(
            listOf("Mang CCCD", "Sạc dự phòng", "Kem chống nắng", "Áo mưa gọn nhẹ"),
            items
        )
    }

    @Test
    fun buildSmartChecklistPrompt_includesWeatherAndProfileContext() {
        val prompt = buildSmartChecklistPrompt(
            profile = TripProfile(
                destination = "Đà Nẵng",
                days = 3,
                budget = "5 triệu",
                people = 2,
                travelStyle = "nghỉ dưỡng",
                transport = "máy bay",
                note = ""
            ),
            days = emptyList(),
            weatherDays = listOf(
                WeatherDay(
                    date = "2026-05-21",
                    tempMinC = 25,
                    tempMaxC = 33,
                    conditionEmoji = "☀️",
                    conditionLabel = "Nắng",
                    rainChancePct = 20
                )
            )
        )

        assertTrue(prompt.contains("Đà Nẵng"))
        assertTrue(prompt.contains("5 triệu"))
        assertTrue(prompt.contains("Nắng"))
    }
}
