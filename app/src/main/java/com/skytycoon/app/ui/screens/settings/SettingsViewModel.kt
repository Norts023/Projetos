package com.skytycoon.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytycoon.app.domain.model.AppSettings
import com.skytycoon.app.domain.usecase.GetAppSettingsUseCase
import com.skytycoon.app.domain.usecase.UpdateAppSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val isSaving: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getAppSettingsUseCase: GetAppSettingsUseCase,
    private val updateAppSettingsUseCase: UpdateAppSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        getAppSettingsUseCase()
            .onEach { settings -> _uiState.update { it.copy(settings = settings) } }
            .launchIn(viewModelScope)
    }

    fun onLanguageChange(code: String) {
        saveSettings(_uiState.value.settings.copy(languageCode = code))
    }

    fun onAutoTimeChange(enabled: Boolean) {
        saveSettings(_uiState.value.settings.copy(defaultAutoTime = enabled))
    }

    fun onSpeedMultiplierChange(multiplier: Int) {
        saveSettings(_uiState.value.settings.copy(defaultSpeedMultiplier = multiplier))
    }

    private fun saveSettings(settings: AppSettings) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            updateAppSettingsUseCase(settings)
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}
