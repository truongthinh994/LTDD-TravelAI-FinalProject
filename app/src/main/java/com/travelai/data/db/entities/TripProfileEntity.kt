package com.travelai.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "trip_profiles",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TripProfileEntity(
    @PrimaryKey
    val sessionId: Long,
    val destination: String,
    val days: Int,
    val budget: String,
    val people: Int,
    val travelStyle: String,
    val transport: String,
    val note: String,
    val createdAt: Long
)
