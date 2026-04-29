package com.joshfeldman.petrecords.core.data.repository

import com.joshfeldman.petrecords.core.data.local.dao.SearchVisitDao
import com.joshfeldman.petrecords.core.data.local.entity.SearchVisitEntity
import com.joshfeldman.petrecords.core.model.SearchRecordsParams
import com.joshfeldman.petrecords.core.model.SearchVisit
import com.joshfeldman.petrecords.core.network.PetRecordsApi
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class SearchRepository @Inject constructor(
    private val api: PetRecordsApi,
    private val dao: SearchVisitDao,
) {
    val visits: Flow<List<SearchVisit>> = dao.observeAll().map { items -> items.map(SearchVisitEntity::toModel) }

    suspend fun search(params: SearchRecordsParams) {
        val response = api.searchRecords(
            petId = params.petId,
            petName = params.petName,
            service = params.service,
            clinicName = params.clinicName,
            dateFrom = params.dateFrom,
            dateTo = params.dateTo,
            invoiceNumber = params.invoiceNumber,
            minTotal = params.minTotal,
            maxTotal = params.maxTotal,
            text = params.text,
        )
        dao.clear()
        dao.upsertAll(response.data.map { SearchVisitEntity.fromModel(it.toModel()) })
    }
}
