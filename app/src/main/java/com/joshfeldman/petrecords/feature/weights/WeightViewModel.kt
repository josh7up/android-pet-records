package com.joshfeldman.petrecords.feature.weights

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshfeldman.petrecords.core.data.repository.WeightRepository
import com.joshfeldman.petrecords.core.model.WeightPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class WeightViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: WeightRepository,
) : ViewModel() {
    private val petId: String = checkNotNull(savedStateHandle["petId"])

    val uiState: StateFlow<WeightUiState> = repository.observePetWeights(petId)
        .map { points -> WeightUiState(petId = petId, points = points) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeightUiState())

    init {
        viewModelScope.launch {
            runCatching { repository.refresh(petId) }
        }
    }
}

data class WeightUiState(
    val petId: String = "",
    val points: List<WeightPoint> = emptyList(),
)
