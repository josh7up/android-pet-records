package com.joshfeldman.petrecords.feature.search

import app.cash.turbine.test
import com.joshfeldman.petrecords.core.data.repository.SearchRepository
import com.joshfeldman.petrecords.core.model.DocumentRecord
import com.joshfeldman.petrecords.core.model.Pet
import com.joshfeldman.petrecords.core.model.PetSpecies
import com.joshfeldman.petrecords.core.model.SearchRecordsParams
import com.joshfeldman.petrecords.core.model.SearchVisit
import com.joshfeldman.petrecords.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<SearchRepository>()
    private val visitsFlow = MutableStateFlow(emptyList<SearchVisit>())

    @Test
    fun `search groups visits by document and passes text to repository`() = runTest {
        every { repository.visits } returns visitsFlow
        coEvery { repository.search(any()) } returns Unit

        visitsFlow.value = listOf(
            visit(id = "visit-1", petName = "Milo", documentId = "doc-1", invoiceNumber = "INV-1"),
            visit(id = "visit-2", petName = "Otis", documentId = "doc-1", invoiceNumber = "INV-1"),
        )

        val viewModel = SearchViewModel(repository)
        viewModel.updateText("rabies")
        viewModel.search()

        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("rabies", state.text)
            assertEquals(false, state.isLoading)
            assertEquals(1, state.visits.size)
            assertEquals("Milo, Otis", state.visits.single().petNamesLabel)
            assertEquals("doc-1", state.visits.single().id)
        }

        coVerify(exactly = 1) {
            repository.search(match<SearchRecordsParams> { it.text == "rabies" })
        }
    }

    @Test
    fun `search does not group invoice matches across different visit dates`() = runTest {
        every { repository.visits } returns visitsFlow
        coEvery { repository.search(any()) } returns Unit

        visitsFlow.value = listOf(
            visit(id = "visit-1", petName = "Milo", documentId = "", invoiceNumber = "INV-1", visitDate = "2026-05-04"),
            visit(id = "visit-2", petName = "Otis", documentId = "", invoiceNumber = "INV-1", visitDate = "2026-05-05"),
        )

        val viewModel = SearchViewModel(repository)
        viewModel.search()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.visits.size)
            assertEquals(listOf("Milo", "Otis"), state.visits.map { it.petNamesLabel })
            assertEquals(listOf("2026-05-04", "2026-05-05"), state.visits.map { it.visitDate })
        }
    }

    @Test
    fun `search groups invoice matches on the same visit date when no document id exists`() = runTest {
        every { repository.visits } returns visitsFlow
        coEvery { repository.search(any()) } returns Unit

        visitsFlow.value = listOf(
            visit(id = "visit-1", petName = "Milo", documentId = "", invoiceNumber = "INV-1", visitDate = "2026-05-04"),
            visit(id = "visit-2", petName = "Otis", documentId = "", invoiceNumber = "INV-1", visitDate = "2026-05-04"),
        )

        val viewModel = SearchViewModel(repository)
        viewModel.search()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.visits.size)
            assertEquals("Milo, Otis", state.visits.single().petNamesLabel)
            assertEquals("2026-05-04", state.visits.single().visitDate)
            assertEquals("INV-1", state.visits.single().invoiceNumber)
        }
    }

    @Test
    fun `search keeps visits separate when neither document id nor invoice number is present`() = runTest {
        every { repository.visits } returns visitsFlow
        coEvery { repository.search(any()) } returns Unit

        visitsFlow.value = listOf(
            visit(id = "visit-1", petName = "Milo", documentId = "", invoiceNumber = ""),
            visit(id = "visit-2", petName = "Otis", documentId = "", invoiceNumber = ""),
        )

        val viewModel = SearchViewModel(repository)
        viewModel.search()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.visits.size)
            assertEquals(listOf("visit-1", "visit-2"), state.visits.map { it.id })
        }
    }

    private fun visit(
        id: String,
        petName: String,
        documentId: String,
        invoiceNumber: String,
        visitDate: String = "2026-05-04",
    ) = SearchVisit(
        id = id,
        visitDate = visitDate,
        invoiceNumber = invoiceNumber,
        pet = Pet(
            id = "pet-$petName",
            householdId = "house-1",
            name = petName,
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
