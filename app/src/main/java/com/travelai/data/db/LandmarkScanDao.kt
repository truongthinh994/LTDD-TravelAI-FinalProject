package com.travelai.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.travelai.data.db.entities.LandmarkScanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LandmarkScanDao {
    @Query("SELECT * FROM landmark_scans ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<LandmarkScanEntity>>

    @Query("SELECT * FROM landmark_scans WHERE id = :id")
    suspend fun getById(id: Long): LandmarkScanEntity?

    @Insert
    suspend fun insert(scan: LandmarkScanEntity): Long

    @Query("DELETE FROM landmark_scans WHERE id = :id")
    suspend fun deleteById(id: Long)
}
