package com.travelai.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetItemTest {
    @Test
    fun parseBudgetAmount_acceptsFormattedVietnameseCurrency() {
        assertEquals(1_250_000L, parseBudgetAmount("1.250.000 đ"))
        assertEquals(350_000L, parseBudgetAmount("350000"))
    }

    @Test
    fun parseBudgetAmount_understandsCommonShortUnits() {
        assertEquals(5_000_000L, parseBudgetAmount("5 triệu"))
        assertEquals(1_500_000L, parseBudgetAmount("1,5tr"))
        assertEquals(500_000L, parseBudgetAmount("500k"))
    }

    @Test
    fun parseBudgetAmount_rejectsBlankAndZero() {
        assertNull(parseBudgetAmount(""))
        assertNull(parseBudgetAmount("0"))
    }

    @Test
    fun totalAndCategorySummary_sumBudgetItems() {
        val items = listOf(
            budgetItem(amountVnd = 100_000L, category = BudgetCategory.FOOD),
            budgetItem(id = 2L, amountVnd = 250_000L, category = BudgetCategory.TRANSPORT)
        )

        assertEquals(350_000L, items.totalAmountVnd())
        assertEquals(100_000L, items.categoryTotals()[BudgetCategory.FOOD])
        assertEquals(250_000L, items.categoryTotals()[BudgetCategory.TRANSPORT])
    }

    @Test
    fun budgetStatus_reportsRemainingAndOverBudget() {
        val items = listOf(
            budgetItem(amountVnd = 600_000L),
            budgetItem(id = 2L, amountVnd = 500_000L)
        )

        val over = items.budgetStatus(limitVnd = 1_000_000L)
        val under = items.budgetStatus(limitVnd = 1_500_000L)

        assertTrue(over.isOverBudget)
        assertEquals(-100_000L, over.remainingVnd)
        assertFalse(under.isOverBudget)
        assertEquals(400_000L, under.remainingVnd)
    }

    @Test
    fun formatBudgetAmount_appendsVietnameseDongSuffix() {
        assertTrue(formatBudgetAmount(1_000_000L).endsWith("đ"))
    }

    private fun budgetItem(
        id: Long = 1L,
        amountVnd: Long,
        category: BudgetCategory = BudgetCategory.FOOD
    ): BudgetItem = BudgetItem(
        id = id,
        sessionId = 10L,
        category = category,
        title = category.label,
        amountVnd = amountVnd,
        note = "",
        createdAt = 1L,
        updatedAt = 1L
    )
}
