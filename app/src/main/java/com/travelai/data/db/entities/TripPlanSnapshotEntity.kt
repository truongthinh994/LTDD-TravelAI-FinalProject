package com.travelai.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "trip_plan_snapshots",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TripPlanSnapshotEntity(
    @PrimaryKey
    val sessionId: Long,
    val rawResponse: String,
    val parsedJson: String?,
    val createdAt: Long,
    val updatedAt: Long
)
