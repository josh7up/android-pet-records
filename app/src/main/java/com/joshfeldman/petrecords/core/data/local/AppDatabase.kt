package com.joshfeldman.petrecords.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.joshfeldman.petrecords.core.data.local.dao.PendingUploadDao
import com.joshfeldman.petrecords.core.data.local.dao.PetDao
import com.joshfeldman.petrecords.core.data.local.dao.SearchVisitDao
import com.joshfeldman.petrecords.core.data.local.dao.WeightDao
import com.joshfeldman.petrecords.core.data.local.entity.PendingUploadEntity
import com.joshfeldman.petrecords.core.data.local.entity.PetEntity
import com.joshfeldman.petrecords.core.data.local.entity.SearchVisitEntity
import com.joshfeldman.petrecords.core.data.local.entity.WeightPointEntity

@Database(
    entities = [PetEntity::class, SearchVisitEntity::class, WeightPointEntity::class, PendingUploadEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun searchVisitDao(): SearchVisitDao
    abstract fun weightDao(): WeightDao
    abstract fun pendingUploadDao(): PendingUploadDao
}
