package com.skytycoon.app.ui.screens.newgame

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytycoon.app.domain.model.GameMode
import com.skytycoon.app.domain.usecase.SeedDataUseCase
import com.skytycoon.app.domain.usecase.StartNewGameUseCase
import com.skytycoon.app.domain.usecase.UseCaseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewGameUiState(
    val companyName: String = "",
    val selectedMode: GameMode = GameMode.REALISTIC,
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val navigateToDashboard: Boolean = false
)

@HiltViewModel
class NewGameViewModel @Inject constructor(
    private val startNewGameUseCase: StartNewGameUseCase,
    private val seedDataUseCase: SeedDataUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewGameUiState())
    val uiState: StateFlow<NewGameUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            seedDataUseCase.invoke(context)
        }
    }

    fun onCompanyNameChange(name: String) {
        _uiState.update { it.copy(companyName = name, errorMsg = null) }
    }

    fun onModeChange(mode: GameMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }

    fun onStartGame() {
        val current = _uiState.value
        if (current.companyName.isBlank()) {
            _uiState.update { it.copy(errorMsg = "Company name cannot be empty") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null) }
            when (val result = startNewGameUseCase.invoke(
                companyName = current.companyName,
                mode = current.selectedMode
            )) {
                is UseCaseResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, navigateToDashboard = true) }
                }
                is UseCaseResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, errorMsg = result.message) }
                }
            }
        }
    }

    fun onNavigated() {
        _uiState.update { it.copy(navigateToDashboard = false) }
    }
}
