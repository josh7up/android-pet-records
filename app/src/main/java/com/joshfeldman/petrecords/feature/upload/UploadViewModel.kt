package com.joshfeldman.petrecords.feature.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshfeldman.petrecords.core.data.repository.UploadRepository
import com.joshfeldman.petrecords.core.model.PendingUpload
import com.joshfeldman.petrecords.core.model.UploadDraft
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val repository: UploadRepository,
) : ViewModel() {
    private val selectedUris = MutableStateFlow<List<String>>(emptyList())
    private val petId = MutableStateFlow("")
    private val visitDate = MutableStateFlow("")
    private val ocrPageCount = MutableStateFlow("")

    val uiState: StateFlow<UploadUiState> = combine(
        selectedUris,
        petId,
        visitDate,
        ocrPageCount,
        repository.uploads,
    ) { uris, pet, date, pageCount, uploads ->
        UploadUiState(
            selectedUris = uris,
            petId = pet,
            visitDate = date,
            ocrPageCount = pageCount,
            uploads = uploads,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UploadUiState())

    fun setPetId(value: String) { petId.value = value }
    fun setVisitDate(value: String) { visitDate.value = value }
    fun setOcrPageCount(value: String) { ocrPageCount.value = value }
    fun replaceSelected(value: List<String>) { selectedUris.value = value }
    fun appendSelected(value: String) { selectedUris.value = selectedUris.value + value }
    fun removeSelected(value: String) { selectedUris.value = selectedUris.value - value }

    fun queueUpload() {
        val uris = selectedUris.value
        if (uris.isEmpty()) return
        viewModelScope.launch {
            repository.enqueue(
                UploadDraft(
                    petId = petId.value.ifBlank { null },
                    visitDate = visitDate.value.ifBlank { null },
                    ocrPageCount = ocrPageCount.value.toIntOrNull(),
                    localUris = uris,
                ),
            )
            selectedUris.value = emptyList()
            ocrPageCount.value = ""
        }
    }
}

data class UploadUiState(
    val selectedUris: List<String> = emptyList(),
    val petId: String = "",
    val visitDate: String = "",
    val ocrPageCount: String = "",
    val uploads: List<PendingUpload> = emptyList(),
)
