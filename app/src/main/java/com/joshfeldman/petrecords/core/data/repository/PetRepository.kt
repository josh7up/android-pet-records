package com.joshfeldman.petrecords.core.data.repository

import com.joshfeldman.petrecords.core.data.local.dao.PetDao
import com.joshfeldman.petrecords.core.data.local.entity.PetEntity
import com.joshfeldman.petrecords.core.model.Pet
import com.joshfeldman.petrecords.core.model.PetSpecies
import com.joshfeldman.petrecords.core.network.PetRecordsApi
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class PetRepository @Inject constructor(
    private val api: PetRecordsApi,
    private val petDao: PetDao,
) {
    val pets: Flow<List<Pet>> = petDao.observeAll().map { items -> items.map(PetEntity::toModel) }

    fun observePet(petId: String): Flow<Pet?> = petDao.observeById(petId).map { it?.toModel() }

    suspend fun refresh(query: String = "", species: PetSpecies? = null) {
        val response = api.getPets(query = query.ifBlank { null }, species = species?.name)
        petDao.clear()
        petDao.upsertAll(response.data.map { PetEntity.fromModel(it.toModel()) })
    }
}
