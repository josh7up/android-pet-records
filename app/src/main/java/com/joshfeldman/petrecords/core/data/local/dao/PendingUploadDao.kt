package com.joshfeldman.petrecords.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.joshfeldman.petrecords.core.data.local.entity.PendingUploadEntity
import com.joshfeldman.petrecords.core.model.UploadState
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingUploadDao {
    @Query("SELECT * FROM pending_uploads ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PendingUploadEntity>>

    @Query("SELECT * FROM pending_uploads WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PendingUploadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: PendingUploadEntity)

    @Query("UPDATE pending_uploads SET state = :state, errorMessage = :errorMessage WHERE id = :id")
    suspend fun updateState(id: String, state: UploadState, errorMessage: String?)

    @Query("DELETE FROM pending_uploads WHERE id = :id")
    suspend fun delete(id: String)
}
