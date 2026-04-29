package com.joshfeldman.petrecords.feature.upload

import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joshfeldman.petrecords.core.navigation.createTempImageUri

@Composable
fun UploadRoute(
    onMessage: (String) -> Unit = {},
    viewModel: UploadViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }

    val pickDocuments = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri ->
            context.contentResolver.takePersistableUriPermission(uri, FLAG_GRANT_READ_URI_PERMISSION)
        }
        viewModel.replaceSelected(uris.map(Uri::toString))
        if (uris.isNotEmpty()) onMessage("Added ${uris.size} file(s)")
    }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = pendingCaptureUri
        if (success && uri != null) {
            viewModel.appendSelected(uri.toString())
            onMessage("Captured page")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Upload receipt", style = MaterialTheme.typography.headlineMedium)
        Text("Pick an existing PDF/image bundle or capture receipt pages with the camera.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { pickDocuments.launch(arrayOf("application/pdf", "image/*")) }) {
                Text("Pick files")
            }
            Button(onClick = {
                val uri = createTempImageUri(context)
                pendingCaptureUri = uri
                takePicture.launch(uri)
            }) {
                Text("Capture page")
            }
        }
        OutlinedTextField(
            value = uiState.petId,
            onValueChange = viewModel::setPetId,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Pet ID") },
        )
        OutlinedTextField(
            value = uiState.visitDate,
            onValueChange = viewModel::setVisitDate,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Visit date (YYYY-MM-DD)") },
        )
        OutlinedTextField(
            value = uiState.ocrPageCount,
            onValueChange = viewModel::setOcrPageCount,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("OCR first N pages") },
        )
        Text("Selected files: ${uiState.selectedUris.size}")
        LazyColumn(
            modifier = Modifier.height(180.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(uiState.selectedUris, key = { it }) { rawUri ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(rawUri.toUri().lastPathSegment ?: rawUri)
                        Button(onClick = { viewModel.removeSelected(rawUri) }) { Text("Remove") }
                    }
                }
            }
        }
        Button(
            onClick = {
                viewModel.queueUpload()
                onMessage("Upload queued")
            },
            enabled = uiState.selectedUris.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Queue upload")
        }
        Text("Upload queue", style = MaterialTheme.typography.titleMedium)
        LazyColumn(
            modifier = Modifier.height(180.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(uiState.uploads, key = { it.id }) { upload ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(upload.id, style = MaterialTheme.typography.labelMedium)
                        Text("${upload.localUris.size} file(s) • ${upload.state}")
                        if (!upload.errorMessage.isNullOrBlank()) {
                            Text(upload.errorMessage)
                        }
                    }
                }
            }
        }
    }
}
