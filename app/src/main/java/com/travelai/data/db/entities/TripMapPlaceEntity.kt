package com.travelai.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trip_map_places",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["sessionId", "dayNumber", "period", "name"], unique = true)
    ]
)
data class TripMapPlaceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val dayNumber: Int,
    val period: String,
    val name: String,
    val query: String,
    val latitude: Double?,
    val longitude: Double?,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long
)
