package com.joshfeldman.petrecords.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshfeldman.petrecords.core.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val draftToken = MutableStateFlow("")

    val uiState: StateFlow<SettingsUiState> = combine(authRepository.token, draftToken) { token, draft ->
        SettingsUiState(currentToken = token, draftToken = if (draft.isBlank()) token else draft)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun updateDraft(value: String) {
        draftToken.value = value
    }

    fun save() {
        viewModelScope.launch {
            authRepository.saveToken(draftToken.value)
        }
    }
}

data class SettingsUiState(
    val currentToken: String = "",
    val draftToken: String = "",
)
