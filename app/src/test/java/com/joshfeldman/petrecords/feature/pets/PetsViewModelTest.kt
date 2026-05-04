package com.joshfeldman.petrecords.feature.pets

import app.cash.turbine.test
import com.joshfeldman.petrecords.core.data.repository.PetRepository
import com.joshfeldman.petrecords.core.model.Pet
import com.joshfeldman.petrecords.core.model.PetSpecies
import com.joshfeldman.petrecords.testutil.MainDispatcherRule
import io.mockk.clearMocks
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
class PetsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<PetRepository>()
    private val petsFlow = MutableStateFlow(emptyList<Pet>())

    @Test
    fun `refresh uses latest query and exposes repository pets`() = runTest {
        every { repository.pets } returns petsFlow
        coEvery { repository.refresh(any(), any()) } returns Unit

        val viewModel = PetsViewModel(repository)
        advanceUntilIdle()
        clearMocks(repository, answers = false, recordedCalls = true)

        val expectedPets = listOf(
            Pet(id = "pet-1", householdId = "house-1", name = "Milo", species = PetSpecies.CAT),
        )

        viewModel.updateQuery("milo")
        petsFlow.value = expectedPets
        viewModel.refresh()

        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("milo", state.query)
            assertEquals(expectedPets, state.pets)
            assertEquals(false, state.isLoading)
        }

        coVerify(exactly = 1) { repository.refresh("milo", null) }
    }
}
