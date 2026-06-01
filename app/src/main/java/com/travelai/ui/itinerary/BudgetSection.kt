package com.travelai.ui.itinerary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.travelai.data.model.BudgetCategory
import com.travelai.data.model.BudgetItem
import com.travelai.data.model.budgetStatus
import com.travelai.data.model.categoryTotals
import com.travelai.data.model.formatBudgetAmount
import com.travelai.ui.theme.DangerRed
import com.travelai.ui.theme.ElectricPurple
import com.travelai.ui.theme.GlassBorder
import com.travelai.ui.theme.GlassSurface
import com.travelai.ui.theme.SuccessGreen
import com.travelai.ui.theme.TextSecondary

@Composable
fun BudgetSection(
    budgetItems: List<BudgetItem>,
    formState: BudgetFormState,
    errorMessage: String?,
    isSaving: Boolean,
    onCategoryChange: (BudgetCategory) -> Unit,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onEdit: (BudgetItem) -> Unit,
    onDelete: (BudgetItem) -> Unit,
    onCancelEdit: () -> Unit,
    modifier: Modifier = Modifier,
    budgetLimitVnd: Long? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BudgetSummary(
            budgetItems = budgetItems,
            budgetLimitVnd = budgetLimitVnd
        )
        BudgetForm(
            formState = formState,
            errorMessage = errorMessage,
            isSaving = isSaving,
            onCategoryChange = onCategoryChange,
            onTitleChange = onTitleChange,
            onAmountChange = onAmountChange,
            onNoteChange = onNoteChange,
            onSave = onSave,
            onCancelEdit = onCancelEdit
        )
        BudgetItemList(
            budgetItems = budgetItems,
            onEdit = onEdit,
            onDelete = onDelete
        )
    }
}

@Composable
private fun BudgetSummary(
    budgetItems: List<BudgetItem>,
    budgetLimitVnd: Long?,
    modifier: Modifier = Modifier
) {
    val status = remember(budgetItems, budgetLimitVnd) {
        budgetItems.budgetStatus(budgetLimitVnd)
    }
    val categoryTotals = remember(budgetItems) { budgetItems.categoryTotals() }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = GlassSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Ngân sách",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Tổng dự kiến: ${formatBudgetAmount(status.totalVnd)}",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            status.remainingVnd?.let { remaining ->
                val message = if (status.isOverBudget) {
                    "Vượt ngân sách khoảng ${formatBudgetAmount(-remaining)}"
                } else {
                    "Còn lại khoảng ${formatBudgetAmount(remaining)}"
                }
                Text(
                    text = message,
                    color = if (status.isOverBudget) DangerRed else SuccessGreen,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (categoryTotals.isNotEmpty()) {
                CategoryBudgetChart(
                    totals = categoryTotals,
                    maxTotal = categoryTotals.values.maxOrNull() ?: 0L
                )
            }
        }
    }
}

@Composable
private fun CategoryBudgetChart(
    totals: Map<BudgetCategory, Long>,
    maxTotal: Long
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BudgetCategory.entries.forEach { category ->
            val total = totals[category].orEmptyAmount()
            if (total <= 0L) return@forEach
            val fraction = if (maxTotal <= 0L) 0f else (total.toFloat() / maxTotal).coerceIn(0.08f, 1f)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category.label,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = formatBudgetAmount(total),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(GlassBorder.copy(alpha = 0.35f), RoundedCornerShape(100))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(8.dp)
                            .background(ElectricPurple, RoundedCornerShape(100))
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetForm(
    formState: BudgetFormState,
    errorMessage: String?,
    isSaving: Boolean,
    onCategoryChange: (BudgetCategory) -> Unit,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancelEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = remember { BudgetCategory.entries.toList() }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = GlassSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (formState.editingItemId == null) "Thêm khoản chi" else "Sửa khoản chi",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(
                    items = categories,
                    key = { category -> category.name }
                ) { category ->
                    CategoryButton(
                        category = category,
                        selected = formState.category == category,
                        onClick = { onCategoryChange(category) }
                    )
                }
            }
            OutlinedTextField(
                value = formState.title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tên khoản chi") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = GlassBorder,
                    unfocusedBorderColor = GlassBorder
                )
            )
            OutlinedTextField(
                value = formState.amountText,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Số tiền dự kiến") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = GlassBorder,
                    unfocusedBorderColor = GlassBorder
                )
            )
            OutlinedTextField(
                value = formState.note,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Ghi chú") },
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = GlassBorder,
                    unfocusedBorderColor = GlassBorder
                )
            )
            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (formState.editingItemId != null) {
                    TextButton(
                        onClick = onCancelEdit,
                        enabled = !isSaving
                    ) {
                        Text("Hủy")
                    }
                }
                Button(
                    onClick = onSave,
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricPurple
                    ),
                    shape = RoundedCornerShape(100)
                ) {
                    Text(if (formState.editingItemId == null) "Thêm" else "Lưu")
                }
            }
        }
    }
}

@Composable
private fun CategoryButton(
    category: BudgetCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(100),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
        ) {
            Text(category.label)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(100),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Text(category.label, color = TextSecondary)
        }
    }
}

@Composable
private fun BudgetItemList(
    budgetItems: List<BudgetItem>,
    onEdit: (BudgetItem) -> Unit,
    onDelete: (BudgetItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (budgetItems.isEmpty()) {
            Text(
                text = "Chưa có khoản chi nào.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            return@Column
        }

        budgetItems.forEach { item ->
            BudgetItemCard(
                item = item,
                onEdit = { onEdit(item) },
                onDelete = { onDelete(item) }
            )
        }
    }
}

@Composable
private fun BudgetItemCard(
    item: BudgetItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.category.label,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = formatBudgetAmount(item.amountVnd),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            if (item.note.isNotBlank()) {
                Text(
                    text = item.note,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Text("Sửa")
                }
                TextButton(onClick = onDelete) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

private fun Long?.orEmptyAmount(): Long = this ?: 0L
