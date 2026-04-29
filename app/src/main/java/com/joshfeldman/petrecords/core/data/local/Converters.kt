package com.joshfeldman.petrecords.core.data.local

import androidx.room.TypeConverter
import com.joshfeldman.petrecords.core.model.PetSpecies
import com.joshfeldman.petrecords.core.model.UploadState

class Converters {
    @TypeConverter
    fun fromPetSpecies(value: PetSpecies): String = value.name

    @TypeConverter
    fun toPetSpecies(value: String): PetSpecies = PetSpecies.valueOf(value)

    @TypeConverter
    fun fromUploadState(value: UploadState): String = value.name

    @TypeConverter
    fun toUploadState(value: String): UploadState = UploadState.valueOf(value)
}
