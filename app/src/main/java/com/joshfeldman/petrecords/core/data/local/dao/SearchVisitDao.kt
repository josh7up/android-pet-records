package com.joshfeldman.petrecords.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.joshfeldman.petrecords.core.data.local.entity.SearchVisitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchVisitDao {
    @Query("SELECT * FROM search_visits ORDER BY visitDate DESC")
    fun observeAll(): Flow<List<SearchVisitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<SearchVisitEntity>)

    @Query("DELETE FROM search_visits")
    suspend fun clear()
}
