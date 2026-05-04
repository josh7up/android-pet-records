package com.joshfeldman.petrecords.feature.record

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshfeldman.petrecords.core.data.repository.SearchRepository
import com.joshfeldman.petrecords.core.model.SearchVisit
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class RecordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SearchRepository,
) : ViewModel() {
    private val recordId: String = checkNotNull(savedStateHandle["recordId"])

    private val _uiState = MutableStateFlow<RecordUiState>(RecordUiState.Loading)
    val uiState: StateFlow<RecordUiState> = _uiState

    init {
        loadRecord()
    }

    private fun loadRecord() {
        viewModelScope.launch {
            _uiState.value = RecordUiState.Loading
            repository.getRecord(recordId).fold(
                onSuccess = { record ->
                    val docId = record.document.id
                    if (docId.isNotBlank()) {
                        repository.downloadDocument(docId).fold(
                            onSuccess = { file ->
                                _uiState.value = RecordUiState.Success(record, file)
                            },
                            onFailure = { error ->
                                _uiState.value = RecordUiState.Error(error.message ?: "Failed to download document")
                            }
                        )
                    } else {
                        _uiState.value = RecordUiState.Success(record, null)
                    }
                },
                onFailure = { error ->
                    _uiState.value = RecordUiState.Error(error.message ?: "Failed to load record")
                }
            )
        }
    }

    fun deleteRecord(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteRecord(recordId).onSuccess {
                onDeleted()
            }
        }
    }
}

sealed interface RecordUiState {
    data object Loading : RecordUiState
    data class Success(val record: SearchVisit, val docFile: File?) : RecordUiState
    data class Error(val message: String) : RecordUiState
}
