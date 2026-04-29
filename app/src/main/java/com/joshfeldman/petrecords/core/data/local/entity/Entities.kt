package com.joshfeldman.petrecords.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.joshfeldman.petrecords.core.model.Pet
import com.joshfeldman.petrecords.core.model.PetSpecies
import com.joshfeldman.petrecords.core.model.SearchVisit
import com.joshfeldman.petrecords.core.model.UploadState
import com.joshfeldman.petrecords.core.model.WeightPoint

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey val id: String,
    val householdId: String,
    val name: String,
    val species: PetSpecies,
    val breed: String?,
    val sex: String?,
) {
    fun toModel() = Pet(id, householdId, name, species, breed, sex)

    companion object {
        fun fromModel(model: Pet) = PetEntity(
            id = model.id,
            householdId = model.householdId,
            name = model.name,
            species = model.species,
            breed = model.breed,
            sex = model.sex,
        )
    }
}

@Entity(tableName = "search_visits")
data class SearchVisitEntity(
    @PrimaryKey val id: String,
    val visitDate: String,
    val invoiceNumber: String?,
    val totalCharges: String?,
    val totalPayments: String?,
    val petId: String,
    val petName: String,
    val petSpecies: PetSpecies,
    val documentId: String,
    val documentName: String,
    val lineItemsSummary: String,
) {
    fun toModel() = SearchVisit(
        id = id,
        visitDate = visitDate,
        invoiceNumber = invoiceNumber,
        totalCharges = totalCharges,
        totalPayments = totalPayments,
        pet = Pet(
            id = petId,
            householdId = "",
            name = petName,
            species = petSpecies,
        ),
        lineItems = emptyList(),
        reminders = emptyList(),
        document = com.joshfeldman.petrecords.core.model.DocumentRecord(
            id = documentId,
            householdId = "",
            originalName = documentName,
            uploadedAt = "",
            ocrStatus = "",
        ),
    )

    companion object {
        fun fromModel(model: SearchVisit) = SearchVisitEntity(
            id = model.id,
            visitDate = model.visitDate,
            invoiceNumber = model.invoiceNumber,
            totalCharges = model.totalCharges,
            totalPayments = model.totalPayments,
            petId = model.pet.id,
            petName = model.pet.name,
            petSpecies = model.pet.species,
            documentId = model.document.id,
            documentName = model.document.originalName,
            lineItemsSummary = model.lineItems.joinToString { it.description },
        )
    }
}

@Entity(tableName = "weight_points")
data class WeightPointEntity(
    @PrimaryKey val id: String,
    val petId: String,
    val measuredAt: String,
    val weightValue: Double,
    val weightUnit: String,
) {
    fun toModel() = WeightPoint(id, measuredAt, weightValue, weightUnit)

    companion object {
        fun fromModel(petId: String, model: WeightPoint) = WeightPointEntity(
            id = model.id,
            petId = petId,
            measuredAt = model.measuredAt,
            weightValue = model.weightValue,
            weightUnit = model.weightUnit,
        )
    }
}

@Entity(tableName = "pending_uploads")
data class PendingUploadEntity(
    @PrimaryKey val id: String,
    val localUris: String,
    val petId: String?,
    val clinicId: String?,
    val visitDate: String?,
    val ocrPageCount: Int?,
    val state: UploadState,
    val errorMessage: String?,
    val createdAt: Long,
)
