package com.joshfeldman.petrecords.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshfeldman.petrecords.core.data.repository.SearchRepository
import com.joshfeldman.petrecords.core.model.SearchRecordsParams
import com.joshfeldman.petrecords.core.model.SearchVisit
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchRepository,
) : ViewModel() {
    private val text = MutableStateFlow("")
    private val loading = MutableStateFlow(false)

    val uiState: StateFlow<SearchUiState> = combine(repository.visits, text, loading) { visits, currentText, isLoading ->
        SearchUiState(currentText, isLoading, visits)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    fun updateText(value: String) {
        text.value = value
    }

    fun search() {
        viewModelScope.launch {
            loading.value = true
            runCatching { repository.search(SearchRecordsParams(text = text.value)) }
            loading.value = false
        }
    }
}

data class SearchUiState(
    val text: String = "",
    val isLoading: Boolean = false,
    val visits: List<SearchVisit> = emptyList(),
)
