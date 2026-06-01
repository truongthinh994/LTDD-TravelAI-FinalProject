package com.travelai.ui.itinerary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.travelai.data.model.ChecklistItem
import com.travelai.data.model.completedChecklistCount
import com.travelai.ui.theme.ElectricPurple
import com.travelai.ui.theme.GlassBorder
import com.travelai.ui.theme.GlassSurface
import com.travelai.ui.theme.TravelAITheme

@Composable
fun ChecklistSection(
    checklistItems: List<ChecklistItem>,
    draftTitle: String,
    errorMessage: String?,
    isSaving: Boolean,
    isGeneratingSmartChecklist: Boolean = false,
    onDraftTitleChange: (String) -> Unit,
    onAdd: () -> Unit,
    onGenerateSmartChecklist: () -> Unit = {},
    onToggle: (ChecklistItem, Boolean) -> Unit,
    onDelete: (ChecklistItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ChecklistSummary(checklistItems = checklistItems)
        ChecklistForm(
            draftTitle = draftTitle,
            errorMessage = errorMessage,
            isSaving = isSaving,
            isGeneratingSmartChecklist = isGeneratingSmartChecklist,
            onDraftTitleChange = onDraftTitleChange,
            onAdd = onAdd,
            onGenerateSmartChecklist = onGenerateSmartChecklist
        )
        ChecklistItemList(
            checklistItems = checklistItems,
            isSaving = isSaving,
            onToggle = onToggle,
            onDelete = onDelete
        )
    }
}

@Composable
private fun ProgressBar(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (total > 0) completed.toFloat() / total else 0f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(GlassSurface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(ElectricPurple, ElectricPurple.copy(alpha = 0.7f))
                    )
                )
                .clip(RoundedCornerShape(3.dp))
        )
    }
}

@Composable
private fun ChecklistSummary(
    checklistItems: List<ChecklistItem>,
    modifier: Modifier = Modifier
) {
    val completedCount = remember(checklistItems) { checklistItems.completedChecklistCount() }
    val totalCount = checklistItems.size

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = GlassSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Checklist chuẩn bị",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$completedCount/$totalCount",
                    color = ElectricPurple,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            if (totalCount > 0) {
                ProgressBar(
                    completed = completedCount,
                    total = totalCount
                )
            }
            Text(
                text = "Đã xong $completedCount/${totalCount} việc",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ChecklistForm(
    draftTitle: String,
    errorMessage: String?,
    isSaving: Boolean,
    isGeneratingSmartChecklist: Boolean,
    onDraftTitleChange: (String) -> Unit,
    onAdd: () -> Unit,
    onGenerateSmartChecklist: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            OutlinedTextField(
                value = draftTitle,
                onValueChange = onDraftTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Việc cần chuẩn bị") },
                placeholder = { Text("Ví dụ: Mang CCCD, sạc dự phòng") },
                singleLine = true,
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onGenerateSmartChecklist,
                    enabled = !isSaving && !isGeneratingSmartChecklist
                ) {
                    Text(if (isGeneratingSmartChecklist) "Đang tạo..." else "Tạo checklist bằng AI")
                }
                Button(
                    onClick = onAdd,
                    enabled = !isSaving && !isGeneratingSmartChecklist,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricPurple
                    ),
                    shape = RoundedCornerShape(100)
                ) {
                    Text("Thêm")
                }
            }
        }
    }
}

@Composable
private fun ChecklistItemList(
    checklistItems: List<ChecklistItem>,
    isSaving: Boolean,
    onToggle: (ChecklistItem, Boolean) -> Unit,
    onDelete: (ChecklistItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (checklistItems.isEmpty()) {
            Text(
                text = "Chưa có việc cần chuẩn bị.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            return@Column
        }

        checklistItems.forEach { item ->
            ChecklistItemCard(
                item = item,
                isSaving = isSaving,
                onToggle = { isChecked -> onToggle(item, isChecked) },
                onDelete = { onDelete(item) }
            )
        }
    }
}

@Composable
private fun ChecklistItemCard(
    item: ChecklistItem,
    isSaving: Boolean,
    onToggle: (Boolean) -> Unit,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = onToggle,
                enabled = !isSaving,
                colors = CheckboxDefaults.colors(
                    checkedColor = ElectricPurple,
                    uncheckedColor = ElectricPurple.copy(alpha = 0.5f)
                )
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.Medium
                    ),
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (item.isChecked) "Đã xong" else "Cần làm",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            TextButton(
                onClick = onDelete,
                enabled = !isSaving
            ) {
                Text("Xóa", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChecklistSectionPreview() {
    TravelAITheme {
        ChecklistSection(
            checklistItems = remember {
                listOf(
                    ChecklistItem(
                        id = 1L,
                        sessionId = 10L,
                        title = "Mang CCCD và giấy tờ đặt phòng",
                        isChecked = true,
                        createdAt = 1L,
                        updatedAt = 2L
                    ),
                    ChecklistItem(
                        id = 2L,
                        sessionId = 10L,
                        title = "Chuẩn bị sạc dự phòng",
                        isChecked = false,
                        createdAt = 3L,
                        updatedAt = 3L
                    )
                )
            },
            draftTitle = "",
            errorMessage = null,
            isSaving = false,
            onDraftTitleChange = {},
            onAdd = {},
            onToggle = { _, _ -> },
            onDelete = {}
        )
    }
}
