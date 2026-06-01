package com.travelai.data.model

data class ChecklistItem(
    val id: Long,
    val sessionId: Long,
    val title: String,
    val isChecked: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

fun List<ChecklistItem>.completedChecklistCount(): Int = count { it.isChecked }
