package com.joshfeldman.petrecords.feature.search

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun SearchRoute(
    onRecordClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Record search", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = uiState.text,
            onValueChange = viewModel::updateText,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Full text, pet name, invoice number, service") },
        )
        Button(onClick = viewModel::search) { Text("Search records") }
        if (uiState.isLoading) {
            CircularProgressIndicator()
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.visits, key = { it.id }) { visit ->
                Card(
                    onClick = { onRecordClick(visit.id) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(visit.petNamesLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(formatVisitDate(visit.visitDate))
                        Text("Invoice: ${visit.invoiceNumber ?: "N/A"}")
                    }
                }
            }
        }
    }
}

private fun formatVisitDate(value: String, locale: Locale = Locale.getDefault()): String =
    parseVisitDate(value)?.let { date ->
        val pattern = DateFormat.getBestDateTimePattern(locale, "yMd")
        SimpleDateFormat(pattern, locale).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(date)
    } ?: value.substringBefore('T')

private fun parseVisitDate(value: String): Date? {
    val datePart = value.substringBefore('T')
    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        isLenient = false
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return runCatching { parser.parse(datePart) }.getOrNull()
}
