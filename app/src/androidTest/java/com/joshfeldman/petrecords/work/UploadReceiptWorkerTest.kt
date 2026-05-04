package com.joshfeldman.petrecords.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import androidx.work.testing.TestListenableWorkerBuilder
import com.joshfeldman.petrecords.core.data.repository.UploadRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class UploadReceiptWorkerTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun doWork_returnsSuccessWhenRepositorySucceeds() = runTest {
        val repository = mockk<UploadRepository>()
        coEvery { repository.processQueuedUpload("upload-1") } returns Result.success(Unit)

        val worker = buildWorker(repository, workDataOf(UploadReceiptWorker.KEY_UPLOAD_ID to "upload-1"))

        assertEquals(ListenableWorker.Result.Success::class.java, worker.doWork()::class.java)
    }

    @Test
    fun doWork_returnsRetryWhenRepositoryFails() = runTest {
        val repository = mockk<UploadRepository>()
        coEvery { repository.processQueuedUpload("upload-1") } returns Result.failure(IllegalStateException("network"))

        val worker = buildWorker(repository, workDataOf(UploadReceiptWorker.KEY_UPLOAD_ID to "upload-1"))

        assertEquals(ListenableWorker.Result.Retry::class.java, worker.doWork()::class.java)
    }

    @Test
    fun doWork_returnsFailureWhenUploadIdIsMissing() = runTest {
        val repository = mockk<UploadRepository>(relaxed = true)
        val worker = buildWorker(repository, workDataOf())

        assertEquals(ListenableWorker.Result.Failure::class.java, worker.doWork()::class.java)
        coVerify(exactly = 0) { repository.processQueuedUpload(any()) }
    }

    private fun buildWorker(
        repository: UploadRepository,
        inputData: androidx.work.Data,
    ): UploadReceiptWorker {
        val workerFactory = object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters,
            ): ListenableWorker? {
                return if (workerClassName == UploadReceiptWorker::class.qualifiedName) {
                    UploadReceiptWorker(appContext, workerParameters, repository)
                } else {
                    null
                }
            }
        }

        return TestListenableWorkerBuilder<UploadReceiptWorker>(context)
            .setWorkerFactory(workerFactory)
            .setInputData(inputData)
            .build()
    }
}
