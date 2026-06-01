package com.travelai.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TripExportTest {
    @Test
    fun toShareText_prefersParsedItineraryAndOmitsChatRoles() {
        val exportText = sampleExport(
            tripPlanSnapshot = TripPlanSnapshot(
                sessionId = 1L,
                rawResponse = "TravelAI: raw chat text",
                days = listOf(
                    TripPlanDay(
                        dayNumber = 1,
                        title = "Sơn Trà",
                        periods = listOf(
                            TripPlanPeriod(
                                period = TripPlanPeriodType.MORNING,
                                content = "Bán đảo Sơn Trà"
                            )
                        )
                    )
                ),
                createdAt = 1L,
                updatedAt = 1L
            )
        ).toShareText()

        assertTrue(exportText.contains("3 ngày Đà Nẵng"))
        assertTrue(exportText.contains("Lịch trình"))
        assertTrue(exportText.contains("Ngày 1: Sơn Trà"))
        assertTrue(exportText.contains("Sáng: Bán đảo Sơn Trà"))
        assertTrue(exportText.contains("Ngân sách dự kiến"))
        assertTrue(exportText.contains("Checklist chuẩn bị"))
        assertFalse(exportText.contains("Bạn:"))
        assertFalse(exportText.contains("TravelAI: raw chat text"))
    }

    @Test
    fun toShareText_usesRawSnapshotWhenParsedItineraryIsMissing() {
        val exportText = sampleExport(
            tripPlanSnapshot = TripPlanSnapshot(
                sessionId = 1L,
                rawResponse = "Lịch trình dạng text chưa parse được",
                days = emptyList(),
                createdAt = 1L,
                updatedAt = 1L
            )
        ).toShareText()

        assertTrue(exportText.contains("Lịch trình dạng text chưa parse được"))
    }

    private fun sampleExport(tripPlanSnapshot: TripPlanSnapshot): TripExport =
        TripExport(
            title = "3 ngày Đà Nẵng",
            tripProfile = TripProfile(
                destination = "Đà Nẵng",
                days = 3,
                budget = "3 triệu",
                people = 2,
                travelStyle = "Tự túc",
                transport = "Xe máy",
                note = ""
            ),
            tripPlanSnapshot = tripPlanSnapshot,
            budgetItems = listOf(
                BudgetItem(
                    id = 1L,
                    sessionId = 1L,
                    category = BudgetCategory.FOOD,
                    title = "Ăn uống",
                    amountVnd = 500_000L,
                    note = "",
                    createdAt = 1L,
                    updatedAt = 1L
                )
            ),
            checklistItems = listOf(
                ChecklistItem(
                    id = 1L,
                    sessionId = 1L,
                    title = "Mang giấy tờ",
                    isChecked = true,
                    createdAt = 1L,
                    updatedAt = 1L
                )
            ),
            fallbackAssistantText = "Bạn: prompt\nTravelAI: fallback"
        )
}
