package com.joshfeldman.petrecords.core.model

import java.util.UUID

enum class PetSpecies {
    DOG, CAT, BIRD, RABBIT, REPTILE, OTHER
}

data class Pet(
    val id: String,
    val householdId: String,
    val name: String,
    val species: PetSpecies,
    val breed: String? = null,
    val sex: String? = null,
)

data class OcrPage(
    val id: String,
    val pageNumber: Int,
    val fullText: String,
)

data class ExtractedField(
    val id: String,
    val fieldName: String,
    val fieldValue: String,
    val confidence: Double? = null,
)

data class PetCandidate(
    val id: String,
    val detectedName: String,
    val normalizedName: String,
    val status: String,
    val matchedPetId: String? = null,
)

data class DocumentRecord(
    val id: String,
    val householdId: String,
    val petId: String? = null,
    val originalName: String,
    val uploadedAt: String,
    val ocrStatus: String,
    val pages: List<OcrPage> = emptyList(),
    val extractedFields: List<ExtractedField> = emptyList(),
    val petCandidates: List<PetCandidate> = emptyList(),
)

data class VisitLineItem(
    val id: String,
    val description: String,
    val totalPrice: String? = null,
    val serviceDate: String? = null,
)

data class Reminder(
    val id: String,
    val serviceName: String,
    val dueDate: String? = null,
    val lastDoneDate: String? = null,
)

data class SearchVisit(
    val id: String,
    val visitDate: String,
    val invoiceNumber: String? = null,
    val totalCharges: String? = null,
    val totalPayments: String? = null,
    val pet: Pet,
    val lineItems: List<VisitLineItem> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
    val document: DocumentRecord,
)

data class WeightPoint(
    val id: String,
    val measuredAt: String,
    val weightValue: Double,
    val weightUnit: String,
)

data class WeightSeries(
    val petId: String,
    val points: List<WeightPoint>,
    val count: Int,
)

data class Paginated<T>(
    val data: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
)

data class SearchRecordsParams(
    val petId: String? = null,
    val petName: String? = null,
    val service: String? = null,
    val clinicName: String? = null,
    val dateFrom: String? = null,
    val dateTo: String? = null,
    val invoiceNumber: String? = null,
    val minTotal: Double? = null,
    val maxTotal: Double? = null,
    val text: String? = null,
)

data class UploadDraft(
    val petId: String? = null,
    val clinicId: String? = null,
    val visitDate: String? = null,
    val ocrPageCount: Int? = null,
    val localUris: List<String> = emptyList(),
)

data class PendingUpload(
    val id: String = UUID.randomUUID().toString(),
    val localUris: List<String>,
    val petId: String? = null,
    val clinicId: String? = null,
    val visitDate: String? = null,
    val ocrPageCount: Int? = null,
    val state: UploadState = UploadState.QUEUED,
    val errorMessage: String? = null,
)

enum class UploadState {
    QUEUED,
    UPLOADING,
    COMPLETE,
    FAILED,
}
