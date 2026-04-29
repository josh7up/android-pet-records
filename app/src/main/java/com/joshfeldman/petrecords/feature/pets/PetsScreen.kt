package com.joshfeldman.petrecords.feature.pets

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PetsRoute(
    onOpenWeightTrend: (String) -> Unit,
    viewModel: PetsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PetsScreen(
        state = uiState,
        onQueryChange = viewModel::updateQuery,
        onSearch = viewModel::refresh,
        onOpenWeightTrend = onOpenWeightTrend,
    )
}

@Composable
private fun PetsScreen(
    state: PetsUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onOpenWeightTrend: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Pets", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search by name") },
        )
        Button(onClick = onSearch) { Text("Search") }
        if (state.isLoading) {
            CircularProgressIndicator()
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.pets, key = { it.id }) { pet ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenWeightTrend(pet.id) },
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(pet.name, style = MaterialTheme.typography.titleMedium)
                        Text("${pet.species}${pet.breed?.let { " • $it" } ?: ""}")
                        Text("Tap to view weight trend")
                    }
                }
            }
        }
    }
}
