package com.joshfeldman.petrecords.core.network

import com.joshfeldman.petrecords.core.model.DocumentRecord
import com.joshfeldman.petrecords.core.model.ExtractedField
import com.joshfeldman.petrecords.core.model.OcrPage
import com.joshfeldman.petrecords.core.model.Paginated
import com.joshfeldman.petrecords.core.model.Pet
import com.joshfeldman.petrecords.core.model.PetCandidate
import com.joshfeldman.petrecords.core.model.PetSpecies
import com.joshfeldman.petrecords.core.model.Reminder
import com.joshfeldman.petrecords.core.model.SearchVisit
import com.joshfeldman.petrecords.core.model.VisitLineItem
import com.joshfeldman.petrecords.core.model.WeightPoint
import com.joshfeldman.petrecords.core.model.WeightSeries

data class PaginatedResponse<T>(
    val data: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
) {
    fun <R> map(transform: (T) -> R): Paginated<R> = Paginated(
        data = data.map(transform),
        total = total,
        page = page,
        pageSize = pageSize,
    )
}

data class PetDto(
    val id: String,
    val householdId: String,
    val name: String,
    val species: PetSpecies,
    val breed: String?,
    val sex: String?,
) {
    fun toModel() = Pet(id, householdId, name, species, breed, sex)
}

data class OcrPageDto(val id: String, val pageNumber: Int, val fullText: String) {
    fun toModel() = OcrPage(id, pageNumber, fullText)
}

data class ExtractedFieldDto(
    val id: String,
    val fieldName: String,
    val fieldValue: String,
    val confidence: Double?,
) {
    fun toModel() = ExtractedField(id, fieldName, fieldValue, confidence)
}

data class PetCandidateDto(
    val id: String,
    val detectedName: String,
    val normalizedName: String,
    val status: String,
    val matchedPetId: String?,
) {
    fun toModel() = PetCandidate(id, detectedName, normalizedName, status, matchedPetId)
}

data class DocumentRecordDto(
    val id: String,
    val householdId: String,
    val petId: String?,
    val originalName: String,
    val uploadedAt: String,
    val ocrStatus: String,
    val pages: List<OcrPageDto>?,
    val extractedFields: List<ExtractedFieldDto>?,
    val petCandidates: List<PetCandidateDto>?,
    val visits: List<SearchVisitDto>?,
) {
    fun toModel() = DocumentRecord(
        id = id,
        householdId = householdId,
        petId = petId,
        originalName = originalName,
        uploadedAt = uploadedAt,
        ocrStatus = ocrStatus,
        pages = pages.orEmpty().map(OcrPageDto::toModel),
        extractedFields = extractedFields.orEmpty().map(ExtractedFieldDto::toModel),
        petCandidates = petCandidates.orEmpty().map(PetCandidateDto::toModel),
    )
}

data class VisitLineItemDto(
    val id: String,
    val description: String,
    val totalPrice: String?,
    val serviceDate: String?,
) {
    fun toModel() = VisitLineItem(id, description, totalPrice, serviceDate)
}

data class ReminderDto(
    val id: String,
    val serviceName: String,
    val dueDate: String?,
    val lastDoneDate: String?,
) {
    fun toModel() = Reminder(id, serviceName, dueDate, lastDoneDate)
}

data class SearchVisitDto(
    val id: String,
    val visitDate: String,
    val invoiceNumber: String?,
    val totalCharges: String?,
    val totalPayments: String?,
    val pet: PetDto,
    val lineItems: List<VisitLineItemDto>?,
    val reminders: List<ReminderDto>?,
    val document: DocumentRecordDto?,
) {
    fun toModel() = SearchVisit(
        id = id,
        visitDate = visitDate,
        invoiceNumber = invoiceNumber,
        totalCharges = totalCharges,
        totalPayments = totalPayments,
        pet = pet.toModel(),
        lineItems = lineItems.orEmpty().map(VisitLineItemDto::toModel),
        reminders = reminders.orEmpty().map(ReminderDto::toModel),
        document = document?.toModel() ?: DocumentRecord(
            id = "",
            householdId = "",
            originalName = "",
            uploadedAt = "",
            ocrStatus = "",
        ),
    )
}

data class WeightPointDto(
    val id: String,
    val measuredAt: String,
    val weightValue: String,
    val weightUnit: String,
) {
    fun toModel() = WeightPoint(id, measuredAt, weightValue.toDoubleOrNull() ?: 0.0, weightUnit)
}

data class WeightStatsDto(val count: Int)

data class WeightSeriesDto(
    val petId: String,
    val points: List<WeightPointDto>,
    val stats: WeightStatsDto,
) {
    fun toModel() = WeightSeries(petId, points.map(WeightPointDto::toModel), stats.count)
}

data class UploadResponse(
    val document: DocumentRecordDto,
    val ocr: OcrJobResult?,
)

data class OcrJobResult(
    val status: String?,
    val message: String?,
)
