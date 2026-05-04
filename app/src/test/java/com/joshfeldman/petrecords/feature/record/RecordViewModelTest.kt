package com.joshfeldman.petrecords.feature.record

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.joshfeldman.petrecords.core.data.repository.SearchRepository
import com.joshfeldman.petrecords.core.model.DocumentRecord
import com.joshfeldman.petrecords.core.model.Pet
import com.joshfeldman.petrecords.core.model.PetSpecies
import com.joshfeldman.petrecords.core.model.SearchVisit
import com.joshfeldman.petrecords.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<SearchRepository>()

    @Test
    fun `loadRecord emits success with downloaded file when document exists`() = runTest {
        val record = record(documentId = "doc-1")
        val file = File("build/tmp/doc-1.pdf")
        coEvery { repository.getRecord("doc-1") } returns Result.success(record)
        coEvery { repository.downloadDocument("doc-1") } returns Result.success(file)
        coEvery { repository.deleteRecord(any()) } returns Result.success(Unit)

        val viewModel = RecordViewModel(savedStateHandle("doc-1"), repository)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is RecordUiState.Success)
            assertEquals(file, (state as RecordUiState.Success).docFile)
            assertEquals(record, state.record)
        }

        coVerify(exactly = 1) { repository.downloadDocument("doc-1") }
    }

    @Test
    fun `loadRecord emits error when download fails`() = runTest {
        val record = record(documentId = "doc-1")
        coEvery { repository.getRecord("doc-1") } returns Result.success(record)
        coEvery { repository.downloadDocument("doc-1") } returns Result.failure(IllegalStateException("boom"))
        coEvery { repository.deleteRecord(any()) } returns Result.success(Unit)

        val viewModel = RecordViewModel(savedStateHandle("doc-1"), repository)
        advanceUntilIdle()

        viewModel.uiState.test {
            assertEquals(RecordUiState.Error("boom"), awaitItem())
        }
    }

    @Test
    fun `loadRecord skips download when document id is blank`() = runTest {
        val record = record(documentId = "")
        coEvery { repository.getRecord("doc-3") } returns Result.success(record)
        coEvery { repository.deleteRecord(any()) } returns Result.success(Unit)

        val viewModel = RecordViewModel(savedStateHandle("doc-3"), repository)
        advanceUntilIdle()

        viewModel.uiState.test {
            assertEquals(RecordUiState.Success(record, null), awaitItem())
        }

        coVerify(exactly = 0) { repository.downloadDocument(any()) }
    }

    @Test
    fun `deleteRecord invokes callback on success`() = runTest {
        val record = record(documentId = "")
        coEvery { repository.getRecord("doc-2") } returns Result.success(record)
        coEvery { repository.deleteRecord("doc-2") } returns Result.success(Unit)

        val viewModel = RecordViewModel(savedStateHandle("doc-2"), repository)
        advanceUntilIdle()

        var deleted = false
        viewModel.deleteRecord { deleted = true }
        advanceUntilIdle()

        assertTrue(deleted)
        coVerify(exactly = 1) { repository.deleteRecord("doc-2") }
    }

    private fun savedStateHandle(recordId: String) = SavedStateHandle(mapOf("recordId" to recordId))

    private fun record(documentId: String) = SearchVisit(
        id = "visit-1",
        visitDate = "2026-05-04",
        pet = Pet(
            id = "pet-1",
            householdId = "house-1",
            name = "Milo",
            species = PetSpecies.CAT,
        ),
        document = DocumentRecord(
            id = documentId,
            householdId = "house-1",
            originalName = "record.pdf",
            uploadedAt = "2026-05-04T00:00:00Z",
            ocrStatus = "complete",
        ),
    )
}
