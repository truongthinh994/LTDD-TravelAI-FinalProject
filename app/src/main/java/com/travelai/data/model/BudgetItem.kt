package com.travelai.data.model

import java.text.NumberFormat
import java.util.Locale

data class BudgetItem(
    val id: Long,
    val sessionId: Long,
    val category: BudgetCategory,
    val title: String,
    val amountVnd: Long,
    val note: String,
    val createdAt: Long,
    val updatedAt: Long
)

enum class BudgetCategory(val label: String) {
    FOOD("Ăn uống"),
    TRANSPORT("Di chuyển"),
    TICKETS("Vé tham quan"),
    HOTEL("Khách sạn"),
    INCIDENTAL("Phát sinh")
}

data class BudgetStatus(
    val totalVnd: Long,
    val limitVnd: Long?,
    val remainingVnd: Long?
) {
    val isOverBudget: Boolean = remainingVnd?.let { it < 0L } == true
}

fun List<BudgetItem>.totalAmountVnd(): Long = sumOf { it.amountVnd }

fun List<BudgetItem>.categoryTotals(): Map<BudgetCategory, Long> =
    groupBy { it.category }.mapValues { (_, items) -> items.totalAmountVnd() }

fun List<BudgetItem>.budgetStatus(limitVnd: Long?): BudgetStatus {
    val total = totalAmountVnd()
    return BudgetStatus(
        totalVnd = total,
        limitVnd = limitVnd,
        remainingVnd = limitVnd?.minus(total)
    )
}

fun parseBudgetAmount(value: String): Long? {
    val normalized = value.lowercase(Locale.forLanguageTag("vi-VN")).trim()
    val multiplier = when {
        normalized.contains("triệu") || normalized.contains("trieu") ||
            normalized.contains("tr") -> 1_000_000L
        normalized.contains("nghìn") || normalized.contains("nghin") ||
            normalized.contains("k") -> 1_000L
        else -> 1L
    }

    val numericText = Regex("""\d+([.,]\d+)?""")
        .find(normalized)
        ?.value
        ?: return null

    val amount = if (multiplier == 1L) {
        normalized.filter { it.isDigit() }.toLongOrNull()
    } else {
        numericText.replace(',', '.').toDoubleOrNull()?.let { (it * multiplier).toLong() }
    }
    return amount?.takeIf { it > 0L }
}

fun formatBudgetAmount(amountVnd: Long): String =
    "${BUDGET_AMOUNT_FORMATTER.format(amountVnd)} đ"

private val BUDGET_AMOUNT_FORMATTER: NumberFormat =
    NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
