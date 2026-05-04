package com.joshfeldman.petrecords.core.data.repository

import android.content.Context
import com.joshfeldman.petrecords.core.data.local.dao.SearchVisitDao
import com.joshfeldman.petrecords.core.data.local.entity.SearchVisitEntity
import com.joshfeldman.petrecords.core.model.SearchRecordsParams
import com.joshfeldman.petrecords.core.model.SearchVisit
import com.joshfeldman.petrecords.core.network.PetRecordsApi
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class SearchRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val api: PetRecordsApi,
    private val dao: SearchVisitDao,
) {
    val visits: Flow<List<SearchVisit>> = dao.observeAll().map { items -> items.map(SearchVisitEntity::toModel) }

    suspend fun search(params: SearchRecordsParams) = withContext(Dispatchers.IO) {
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

    suspend fun getRecord(id: String): Result<SearchVisit> = withContext(Dispatchers.IO) {
        runCatching {
            val doc = api.getDocument(id)
            val visitDto = doc.visits?.firstOrNull() ?: error("No visit found for document")
            visitDto.toModel().copy(document = doc.toModel())
        }
    }

    suspend fun deleteRecord(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            api.deleteDocument(id)
        }
    }

    suspend fun downloadDocument(documentId: String): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.downloadDocument(documentId)
            if (!response.isSuccessful) {
                error("Server returned error ${response.code()}: ${response.message()}")
            }
            val body = response.body() ?: error("Response body is null")
            
            body.use { b ->
                val file = File(context.cacheDir, "doc_${documentId}.pdf")
                b.byteStream().use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                file
            }
        }
    }
}
