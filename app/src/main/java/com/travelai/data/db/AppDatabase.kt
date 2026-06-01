package com.travelai.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.travelai.data.db.entities.BudgetItemEntity
import com.travelai.data.db.entities.ChecklistItemEntity
import com.travelai.data.db.entities.ChatMessageEntity
import com.travelai.data.db.entities.ChatSessionEntity
import com.travelai.data.db.entities.LandmarkScanEntity
import com.travelai.data.db.entities.TripMapPlaceEntity
import com.travelai.data.db.entities.TripPlanSnapshotEntity
import com.travelai.data.db.entities.TripProfileEntity

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        TripProfileEntity::class,
        TripPlanSnapshotEntity::class,
        TripMapPlaceEntity::class,
        BudgetItemEntity::class,
        ChecklistItemEntity::class,
        LandmarkScanEntity::class
    ],
    version = 8,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun landmarkScanDao(): LandmarkScanDao
}
