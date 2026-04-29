package com.joshfeldman.petrecords.core.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.joshfeldman.petrecords.core.data.local.dao.PendingUploadDao
import com.joshfeldman.petrecords.core.data.local.entity.PendingUploadEntity
import com.joshfeldman.petrecords.core.model.PendingUpload
import com.joshfeldman.petrecords.core.model.UploadDraft
import com.joshfeldman.petrecords.core.model.UploadState
import com.joshfeldman.petrecords.core.network.PetRecordsApi
import com.joshfeldman.petrecords.work.UploadReceiptWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class UploadRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val api: PetRecordsApi,
    private val pendingUploadDao: PendingUploadDao,
    private val workManager: WorkManager,
) {
    val uploads: Flow<List<PendingUpload>> = pendingUploadDao.observeAll().map { items ->
        items.map { entity ->
            PendingUpload(
                id = entity.id,
                localUris = entity.localUris.split("|").filter(String::isNotBlank),
                petId = entity.petId,
                clinicId = entity.clinicId,
                visitDate = entity.visitDate,
                ocrPageCount = entity.ocrPageCount,
                state = entity.state,
                errorMessage = entity.errorMessage,
            )
        }
    }

    suspend fun enqueue(draft: UploadDraft) {
        val upload = PendingUpload(
            localUris = draft.localUris,
            petId = draft.petId,
            clinicId = draft.clinicId,
            visitDate = draft.visitDate,
            ocrPageCount = draft.ocrPageCount,
        )
        pendingUploadDao.upsert(
            PendingUploadEntity(
                id = upload.id,
                localUris = upload.localUris.joinToString("|"),
                petId = upload.petId,
                clinicId = upload.clinicId,
                visitDate = upload.visitDate,
                ocrPageCount = upload.ocrPageCount,
                state = upload.state,
                errorMessage = null,
                createdAt = System.currentTimeMillis(),
            ),
        )
        val request = OneTimeWorkRequestBuilder<UploadReceiptWorker>()
            .setInputData(workDataOf(UploadReceiptWorker.KEY_UPLOAD_ID to upload.id))
            .build()
        workManager.enqueue(request)
    }

    suspend fun processQueuedUpload(uploadId: String): Result<Unit> {
        val item = pendingUploadDao.getById(uploadId) ?: return Result.failure(IllegalArgumentException("Upload not found"))
        pendingUploadDao.updateState(uploadId, UploadState.UPLOADING, null)
        return runCatching {
            val uris = item.localUris.split("|").filter(String::isNotBlank).map(String::toUri)
            if (uris.isEmpty()) error("No files selected")
            if (uris.size == 1) {
                api.uploadDocument(
                    file = uris.first().toMultipartPart(context.contentResolver, "file"),
                    petId = item.petId.toRequestPart(),
                    clinicId = item.clinicId.toRequestPart(),
                    visitDate = item.visitDate.toRequestPart(),
                )
            } else {
                api.uploadDocumentImages(
                    files = uris.map { it.toMultipartPart(context.contentResolver, "files") },
                    petId = item.petId.toRequestPart(),
                    clinicId = item.clinicId.toRequestPart(),
                    visitDate = item.visitDate.toRequestPart(),
                    ocrPageCount = item.ocrPageCount?.toString().toRequestPart(),
                )
            }
            pendingUploadDao.updateState(uploadId, UploadState.COMPLETE, null)
            pendingUploadDao.delete(uploadId)
        }.onFailure { error ->
            pendingUploadDao.updateState(uploadId, UploadState.FAILED, error.message)
        }.map {}
    }

    private fun String?.toRequestPart() = this
        ?.takeIf { it.isNotBlank() }
        ?.toRequestBody("text/plain".toMediaTypeOrNull())

    private fun Uri.toMultipartPart(contentResolver: ContentResolver, fieldName: String): MultipartBody.Part {
        val inputStream = requireNotNull(contentResolver.openInputStream(this))
        val mimeType = contentResolver.getType(this) ?: "application/octet-stream"
        val extension = when {
            mimeType.contains("pdf") -> ".pdf"
            mimeType.contains("png") -> ".png"
            else -> ".jpg"
        }
        val tempFile = File.createTempFile("upload_", extension, context.cacheDir)
        inputStream.use { input ->
            FileOutputStream(tempFile).use { output -> input.copyTo(output) }
        }
        val body = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(fieldName, tempFile.name, body)
    }
}
