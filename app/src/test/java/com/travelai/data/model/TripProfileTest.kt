package com.travelai.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TripProfileTest {
    @Test
    fun toSessionTitle_usesDaysAndDestination() {
        val profile = tripProfile(destination = "Đà Nẵng", days = 3)

        assertEquals("3 ngày Đà Nẵng", profile.toSessionTitle())
    }

    @Test
    fun toInitialPrompt_includesStructuredTripFields() {
        val prompt = tripProfile().toInitialPrompt()

        assertTrue(prompt.contains("- Điểm đến: Đà Nẵng"))
        assertTrue(prompt.contains("- Số ngày: 3"))
        assertTrue(prompt.contains("- Số người: 2"))
        assertTrue(prompt.contains("- Ngân sách: 5 triệu / người"))
        assertTrue(prompt.contains("- Phong cách: Ăn uống và nghỉ dưỡng"))
        assertTrue(prompt.contains("- Phương tiện: Taxi"))
        assertTrue(prompt.contains("Sáng / Chiều / Tối"))
        assertTrue(prompt.contains("Google Maps"))
    }

    @Test
    fun toPromptContext_omitsBlankOptionalFields() {
        val context = tripProfile(budget = "", note = "").toPromptContext()

        assertFalse(context.contains("Ngân sách"))
        assertFalse(context.contains("Ghi chú"))
        assertTrue(context.contains("- Điểm đến: Đà Nẵng"))
    }

    private fun tripProfile(
        destination: String = "Đà Nẵng",
        days: Int = 3,
        budget: String = "5 triệu / người",
        people: Int = 2,
        travelStyle: String = "Ăn uống và nghỉ dưỡng",
        transport: String = "Taxi",
        note: String = "Di cung tre nho"
    ): TripProfile = TripProfile(
        destination = destination,
        days = days,
        budget = budget,
        people = people,
        travelStyle = travelStyle,
        transport = transport,
        note = note
    )
}
