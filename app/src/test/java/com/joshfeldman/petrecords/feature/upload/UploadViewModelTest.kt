package com.joshfeldman.petrecords.feature.upload

import app.cash.turbine.test
import com.joshfeldman.petrecords.core.data.repository.UploadRepository
import com.joshfeldman.petrecords.core.model.PendingUpload
import com.joshfeldman.petrecords.core.model.UploadDraft
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
class UploadViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<UploadRepository>()
    private val uploadsFlow = MutableStateFlow(emptyList<PendingUpload>())

    @Test
    fun `queueUpload enqueues draft and clears transient selection state`() = runTest {
        every { repository.uploads } returns uploadsFlow
        coEvery { repository.enqueue(any()) } returns Unit

        val viewModel = UploadViewModel(repository)
        viewModel.setPetId("pet-1")
        viewModel.setVisitDate("2026-05-04")
        viewModel.setOcrPageCount("3")
        viewModel.replaceSelected(listOf("content://one", "content://two"))

        viewModel.queueUpload()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.enqueue(
                match<UploadDraft> {
                    it.petId == "pet-1" &&
                        it.visitDate == "2026-05-04" &&
                        it.ocrPageCount == 3 &&
                        it.localUris == listOf("content://one", "content://two")
                },
            )
        }

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(emptyList<String>(), state.selectedUris)
            assertEquals("", state.ocrPageCount)
            assertEquals("pet-1", state.petId)
            assertEquals("2026-05-04", state.visitDate)
        }
    }

    @Test
    fun `queueUpload does nothing when no files are selected`() = runTest {
        every { repository.uploads } returns uploadsFlow

        val viewModel = UploadViewModel(repository)
        viewModel.setPetId("pet-1")

        viewModel.queueUpload()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(emptyList<String>(), state.selectedUris)
            assertEquals("pet-1", state.petId)
        }

        coVerify(exactly = 0) { repository.enqueue(any()) }
    }

    @Test
    fun `queueUpload converts invalid ocrPageCount to null`() = runTest {
        every { repository.uploads } returns uploadsFlow
        coEvery { repository.enqueue(any()) } returns Unit

        val viewModel = UploadViewModel(repository)
        viewModel.setOcrPageCount("abc")
        viewModel.replaceSelected(listOf("content://one"))

        viewModel.queueUpload()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.enqueue(
                match<UploadDraft> {
                    it.localUris == listOf("content://one") && it.ocrPageCount == null
                },
            )
        }
    }
}
