package com.travelai.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.travelai.data.db.entities.BudgetItemEntity
import com.travelai.data.db.entities.ChecklistItemEntity
import com.travelai.data.db.entities.ChatMessageEntity
import com.travelai.data.db.entities.ChatSessionEntity
import com.travelai.data.db.entities.TripMapPlaceEntity
import com.travelai.data.db.entities.TripPlanSnapshotEntity
import com.travelai.data.db.entities.TripProfileEntity

@Dao
abstract class ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC LIMIT 1")
    abstract suspend fun getLatestSession(): ChatSessionEntity?

    @Query("SELECT * FROM chat_sessions ORDER BY isPinned DESC, updatedAt DESC")
    abstract suspend fun getSessions(): List<ChatSessionEntity>

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId LIMIT 1")
    abstract suspend fun getSession(sessionId: Long): ChatSessionEntity?

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY createdAt ASC, id ASC")
    abstract suspend fun getMessagesForSession(sessionId: Long): List<ChatMessageEntity>

    @Query("SELECT * FROM trip_profiles WHERE sessionId = :sessionId LIMIT 1")
    abstract suspend fun getTripProfile(sessionId: Long): TripProfileEntity?

    @Query("SELECT * FROM trip_plan_snapshots WHERE sessionId = :sessionId LIMIT 1")
    abstract suspend fun getTripPlanSnapshot(sessionId: Long): TripPlanSnapshotEntity?

    @Query("SELECT * FROM trip_map_places WHERE sessionId = :sessionId ORDER BY dayNumber ASC, id ASC")
    abstract suspend fun getTripMapPlaces(sessionId: Long): List<TripMapPlaceEntity>

    @Query("SELECT * FROM budget_items WHERE sessionId = :sessionId ORDER BY createdAt ASC, id ASC")
    abstract suspend fun getBudgetItems(sessionId: Long): List<BudgetItemEntity>

    @Query("SELECT * FROM checklist_items WHERE sessionId = :sessionId ORDER BY createdAt ASC, id ASC")
    abstract suspend fun getChecklistItems(sessionId: Long): List<ChecklistItemEntity>

    @Insert
    abstract suspend fun insertSession(session: ChatSessionEntity): Long

    @Insert
    abstract suspend fun insertMessage(message: ChatMessageEntity): Long

    @Insert
    abstract suspend fun insertTripProfile(profile: TripProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertTripPlanSnapshot(snapshot: TripPlanSnapshotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTripMapPlaces(places: List<TripMapPlaceEntity>)

    @Insert
    abstract suspend fun insertBudgetItem(item: BudgetItemEntity): Long

    @Insert
    abstract suspend fun insertChecklistItem(item: ChecklistItemEntity): Long

    @Query(
        """
        UPDATE budget_items
        SET category = :category,
            title = :title,
            amountVnd = :amountVnd,
            note = :note,
            updatedAt = :updatedAt
        WHERE id = :itemId AND sessionId = :sessionId
        """
    )
    abstract suspend fun updateBudgetItem(
        sessionId: Long,
        itemId: Long,
        category: String,
        title: String,
        amountVnd: Long,
        note: String,
        updatedAt: Long
    )

    @Query("DELETE FROM budget_items WHERE id = :itemId AND sessionId = :sessionId")
    abstract suspend fun deleteBudgetItem(sessionId: Long, itemId: Long)

    @Query("DELETE FROM trip_map_places WHERE sessionId = :sessionId")
    abstract suspend fun deleteTripMapPlacesForSession(sessionId: Long)

    @Query(
        """
        UPDATE trip_map_places
        SET latitude = :latitude,
            longitude = :longitude,
            status = :status,
            updatedAt = :updatedAt
        WHERE id = :placeId AND sessionId = :sessionId
        """
    )
    abstract suspend fun updateTripMapPlaceGeocode(
        sessionId: Long,
        placeId: Long,
        latitude: Double?,
        longitude: Double?,
        status: String,
        updatedAt: Long
    )

    @Query(
        """
        UPDATE checklist_items
        SET isChecked = :isChecked,
            updatedAt = :updatedAt
        WHERE id = :itemId AND sessionId = :sessionId
        """
    )
    abstract suspend fun updateChecklistItemChecked(
        sessionId: Long,
        itemId: Long,
        isChecked: Boolean,
        updatedAt: Long
    )

    @Query("DELETE FROM checklist_items WHERE id = :itemId AND sessionId = :sessionId")
    abstract suspend fun deleteChecklistItem(sessionId: Long, itemId: Long)

    @Query("UPDATE chat_sessions SET updatedAt = :updatedAt WHERE id = :sessionId")
    abstract suspend fun updateSessionUpdatedAt(sessionId: Long, updatedAt: Long)

    @Query(
        """
        UPDATE chat_sessions
        SET title = :title,
            updatedAt = :updatedAt
        WHERE id = :sessionId
        """
    )
    abstract suspend fun renameSession(sessionId: Long, title: String, updatedAt: Long)

    @Query("UPDATE chat_sessions SET isPinned = :isPinned WHERE id = :sessionId")
    abstract suspend fun updateSessionPinned(sessionId: Long, isPinned: Boolean)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    abstract suspend fun deleteSession(sessionId: Long)

    // Transactional wrappers — bundle write + updateSessionUpdatedAt so app crash mid-op
    // can't leave session.updatedAt out of sync with its children.

    @Transaction
    open suspend fun insertSessionAndProfile(
        session: ChatSessionEntity,
        profile: TripProfileEntity
    ): Long {
        val sessionId = insertSession(session)
        insertTripProfile(profile.copy(sessionId = sessionId))
        return sessionId
    }

    @Transaction
    open suspend fun insertMessageAndTouchSession(
        message: ChatMessageEntity,
        updatedAt: Long
    ) {
        insertMessage(message)
        updateSessionUpdatedAt(message.sessionId, updatedAt)
    }

    @Transaction
    open suspend fun replaceTripMapPlacesForSession(
        sessionId: Long,
        places: List<TripMapPlaceEntity>
    ) {
        deleteTripMapPlacesForSession(sessionId)
        if (places.isNotEmpty()) {
            insertTripMapPlaces(places)
        }
    }

    @Transaction
    open suspend fun insertBudgetItemAndTouchSession(
        item: BudgetItemEntity,
        updatedAt: Long
    ): Long {
        val itemId = insertBudgetItem(item)
        updateSessionUpdatedAt(item.sessionId, updatedAt)
        return itemId
    }

    @Transaction
    open suspend fun updateBudgetItemAndTouchSession(
        sessionId: Long,
        itemId: Long,
        category: String,
        title: String,
        amountVnd: Long,
        note: String,
        updatedAt: Long
    ) {
        updateBudgetItem(
            sessionId = sessionId,
            itemId = itemId,
            category = category,
            title = title,
            amountVnd = amountVnd,
            note = note,
            updatedAt = updatedAt
        )
        updateSessionUpdatedAt(sessionId, updatedAt)
    }

    @Transaction
    open suspend fun deleteBudgetItemAndTouchSession(
        sessionId: Long,
        itemId: Long,
        updatedAt: Long
    ) {
        deleteBudgetItem(sessionId = sessionId, itemId = itemId)
        updateSessionUpdatedAt(sessionId, updatedAt)
    }

    @Transaction
    open suspend fun insertChecklistItemAndTouchSession(
        item: ChecklistItemEntity,
        updatedAt: Long
    ): Long {
        val itemId = insertChecklistItem(item)
        updateSessionUpdatedAt(item.sessionId, updatedAt)
        return itemId
    }

    @Transaction
    open suspend fun updateChecklistItemCheckedAndTouchSession(
        sessionId: Long,
        itemId: Long,
        isChecked: Boolean,
        updatedAt: Long
    ) {
        updateChecklistItemChecked(
            sessionId = sessionId,
            itemId = itemId,
            isChecked = isChecked,
            updatedAt = updatedAt
        )
        updateSessionUpdatedAt(sessionId, updatedAt)
    }

    @Transaction
    open suspend fun deleteChecklistItemAndTouchSession(
        sessionId: Long,
        itemId: Long,
        updatedAt: Long
    ) {
        deleteChecklistItem(sessionId = sessionId, itemId = itemId)
        updateSessionUpdatedAt(sessionId, updatedAt)
    }
}
