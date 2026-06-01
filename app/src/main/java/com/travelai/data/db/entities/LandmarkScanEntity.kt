package com.travelai.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "landmark_scans")
data class LandmarkScanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val location: String,
    val description: String,
    val history: String,
    val tipsJson: String,
    val confidence: Float,
    val imagePath: String,
    val createdAt: Long
)
