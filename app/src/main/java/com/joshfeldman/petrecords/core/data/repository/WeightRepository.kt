package com.joshfeldman.petrecords.core.data.repository

import com.joshfeldman.petrecords.core.data.local.dao.WeightDao
import com.joshfeldman.petrecords.core.data.local.entity.WeightPointEntity
import com.joshfeldman.petrecords.core.model.WeightPoint
import com.joshfeldman.petrecords.core.network.PetRecordsApi
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class WeightRepository @Inject constructor(
    private val api: PetRecordsApi,
    private val weightDao: WeightDao,
) {
    fun observePetWeights(petId: String): Flow<List<WeightPoint>> =
        weightDao.observeForPet(petId).map { items -> items.map(WeightPointEntity::toModel) }

    suspend fun refresh(petId: String) {
        val response = api.getWeightSeries(petId)
        weightDao.clearForPet(petId)
        weightDao.upsertAll(response.points.map { WeightPointEntity.fromModel(petId, it.toModel()) })
    }
}
