package com.joshfeldman.petrecords.core.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.joshfeldman.petrecords.core.data.local.entity.PendingUploadEntity
import com.joshfeldman.petrecords.core.data.local.entity.PetEntity
import com.joshfeldman.petrecords.core.data.local.entity.SearchVisitEntity
import com.joshfeldman.petrecords.core.data.local.entity.WeightPointEntity
import com.joshfeldman.petrecords.core.model.PetSpecies
import com.joshfeldman.petrecords.core.model.UploadState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseDaoTest {
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun petDao_observesSortedPetsAndUpdatesById() = runTest {
        database.petDao().upsertAll(
            listOf(
                PetEntity("pet-2", "house-1", "Zelda", PetSpecies.CAT, null, null),
                PetEntity("pet-1", "house-1", "Arlo", PetSpecies.DOG, "Lab", "M"),
            ),
        )

        val pets = database.petDao().observeAll().first()
        val pet = database.petDao().observeById("pet-1").first()

        assertEquals(listOf("Arlo", "Zelda"), pets.map { it.name })
        assertEquals("Arlo", pet?.name)
        assertEquals("Lab", pet?.breed)
    }

    @Test
    fun searchVisitDao_observesNewestVisitFirstAndClearsRows() = runTest {
        database.searchVisitDao().upsertAll(
            listOf(
                searchVisitEntity(id = "visit-older", visitDate = "2026-04-01"),
                searchVisitEntity(id = "visit-newer", visitDate = "2026-05-01"),
            ),
        )

        val initial = database.searchVisitDao().observeAll().first()
        database.searchVisitDao().clear()
        val cleared = database.searchVisitDao().observeAll().first()

        assertEquals(listOf("visit-newer", "visit-older"), initial.map { it.id })
        assertEquals(emptyList<SearchVisitEntity>(), cleared)
    }

    @Test
    fun weightDao_returnsPetSeriesInAscendingOrderAndClearsOnlyRequestedPet() = runTest {
        database.weightDao().upsertAll(
            listOf(
                WeightPointEntity("w-2", "pet-1", "2026-05-02", 26.0, "lb"),
                WeightPointEntity("w-1", "pet-1", "2026-05-01", 25.0, "lb"),
                WeightPointEntity("w-3", "pet-2", "2026-05-03", 10.0, "lb"),
            ),
        )

        val initial = database.weightDao().observeForPet("pet-1").first()
        database.weightDao().clearForPet("pet-1")
        val clearedPet1 = database.weightDao().observeForPet("pet-1").first()
        val untouchedPet2 = database.weightDao().observeForPet("pet-2").first()

        assertEquals(listOf("w-1", "w-2"), initial.map { it.id })
        assertEquals(emptyList<WeightPointEntity>(), clearedPet1)
        assertEquals(listOf("w-3"), untouchedPet2.map { it.id })
    }

    @Test
    fun pendingUploadDao_updatesStateAndDeletesRows() = runTest {
        database.pendingUploadDao().upsert(
            pendingUploadEntity(id = "upload-older", createdAt = 1L),
        )
        database.pendingUploadDao().upsert(
            pendingUploadEntity(id = "upload-newer", createdAt = 2L),
        )

        val initial = database.pendingUploadDao().observeAll().first()
        database.pendingUploadDao().updateState("upload-newer", UploadState.FAILED, "boom")
        val updated = database.pendingUploadDao().getById("upload-newer")
        database.pendingUploadDao().delete("upload-newer")
        val afterDelete = database.pendingUploadDao().getById("upload-newer")

        assertEquals(listOf("upload-newer", "upload-older"), initial.map { it.id })
        assertEquals(UploadState.FAILED, updated?.state)
        assertEquals("boom", updated?.errorMessage)
        assertNull(afterDelete)
    }

    private fun searchVisitEntity(id: String, visitDate: String) = SearchVisitEntity(
        id = id,
        visitDate = visitDate,
        invoiceNumber = "INV-$id",
        totalCharges = "100.00",
        totalPayments = "80.00",
        petId = "pet-1",
        petName = "Arlo",
        petSpecies = PetSpecies.DOG,
        documentId = "doc-$id",
        documentName = "$id.pdf",
        lineItemsSummary = "Exam",
    )

    private fun pendingUploadEntity(id: String, createdAt: Long) = PendingUploadEntity(
        id = id,
        localUris = "content://receipt/$id",
        petId = "pet-1",
        clinicId = "clinic-1",
        visitDate = "2026-05-04",
        ocrPageCount = 2,
        state = UploadState.QUEUED,
        errorMessage = null,
        createdAt = createdAt,
    )
}
