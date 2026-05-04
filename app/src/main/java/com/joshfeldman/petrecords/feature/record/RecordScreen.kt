package com.joshfeldman.petrecords.feature.record

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.pdf.viewer.fragment.PdfViewerFragment
import com.joshfeldman.petrecords.core.model.SearchVisit
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordRoute(
    onBack: () -> Unit,
    viewModel: RecordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.deleteRecord(onBack) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is RecordUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is RecordUiState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                }
                is RecordUiState.Success -> {
                    RecordScreenContent(state.record, state.docFile)
                }
            }
        }
    }
}

@Composable
fun RecordScreenContent(record: SearchVisit, docFile: File?) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (docFile != null) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                PdfViewer(docFile)
            }
        }

        HorizontalDivider()

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("OCR + parsed fields", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                ParsedField("Pet Name", record.pet.name)
                ParsedField("Visit Date", record.visitDate)
                ParsedField("Invoice Number", record.invoiceNumber ?: "N/A")
                ParsedField("Total Charges", record.totalCharges ?: "N/A")
                ParsedField("Total Payments", record.totalPayments ?: "N/A")
            }

            if (record.lineItems.isNotEmpty()) {
                item {
                    Text("Line Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                items(record.lineItems) { item ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.description, modifier = Modifier.weight(1f))
                        Text(item.totalPrice ?: "")
                    }
                }
            }

            if (record.reminders.isNotEmpty()) {
                item {
                    Text("Reminders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                items(record.reminders) { reminder ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(reminder.serviceName, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Due: ${reminder.dueDate ?: "N/A"}")
                            Text("Last: ${reminder.lastDoneDate ?: "N/A"}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParsedField(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", fontWeight = FontWeight.Bold)
        Text(value)
    }
}

@Composable
fun PdfViewer(file: File) {
    val context = LocalContext.current
    val fragmentActivity = context as? FragmentActivity
    if (fragmentActivity == null) {
        Text("PDF Viewer requires FragmentActivity")
        return
    }

    val viewId = remember { android.view.View.generateViewId() }

    AndroidView(
        factory = { ctx ->
            android.widget.FrameLayout(ctx).apply {
                id = viewId
            }
        },
        update = { _ ->
            val fragmentManager = fragmentActivity.supportFragmentManager
            val existingFragment = fragmentManager.findFragmentById(viewId) as? PdfViewerFragment
            if (existingFragment == null) {
                val fragment = PdfViewerFragment()
                fragmentManager.beginTransaction()
                    .replace(viewId, fragment)
                    .runOnCommit {
                        fragment.documentUri = Uri.fromFile(file)
                    }
                    .commit()
            } else {
                if (existingFragment.documentUri != Uri.fromFile(file)) {
                    existingFragment.documentUri = Uri.fromFile(file)
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
