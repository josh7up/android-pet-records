package com.joshfeldman.petrecords.feature.pets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshfeldman.petrecords.core.data.repository.PetRepository
import com.joshfeldman.petrecords.core.model.Pet
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PetsViewModel @Inject constructor(
    private val repository: PetRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val loading = MutableStateFlow(false)

    val uiState: StateFlow<PetsUiState> = combine(repository.pets, query, loading) { pets, currentQuery, isLoading ->
        PetsUiState(
            query = currentQuery,
            isLoading = isLoading,
            pets = pets,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PetsUiState())

    init {
        refresh()
    }

    fun updateQuery(value: String) {
        query.value = value
    }

    fun refresh() {
        viewModelScope.launch {
            loading.value = true
            runCatching { repository.refresh(query.value) }
            loading.value = false
        }
    }
}

data class PetsUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val pets: List<Pet> = emptyList(),
)
