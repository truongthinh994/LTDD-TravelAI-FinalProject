package com.travelai.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ChecklistItemTest {
    @Test
    fun completedChecklistCount_countsOnlyCheckedItems() {
        val items = listOf(
            checklistItem(id = 1L, isChecked = true),
            checklistItem(id = 2L, isChecked = false),
            checklistItem(id = 3L, isChecked = true)
        )

        assertEquals(2, items.completedChecklistCount())
    }

    @Test
    fun completedChecklistCount_returnsZeroForEmptyList() {
        assertEquals(0, emptyList<ChecklistItem>().completedChecklistCount())
    }

    private fun checklistItem(
        id: Long,
        isChecked: Boolean
    ): ChecklistItem = ChecklistItem(
        id = id,
        sessionId = 10L,
        title = "Chuẩn bị hành lý",
        isChecked = isChecked,
        createdAt = 1L,
        updatedAt = 1L
    )
}
