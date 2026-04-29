package com.joshfeldman.petrecords.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.joshfeldman.petrecords.core.data.local.entity.WeightPointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_points WHERE petId = :petId ORDER BY measuredAt ASC")
    fun observeForPet(petId: String): Flow<List<WeightPointEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<WeightPointEntity>)

    @Query("DELETE FROM weight_points WHERE petId = :petId")
    suspend fun clearForPet(petId: String)
}
