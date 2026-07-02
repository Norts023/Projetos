package com.skytycoon.app.ui.screens.fleet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytycoon.app.data.repository.AircraftModelRepository
import com.skytycoon.app.data.repository.GameStateRepository
import com.skytycoon.app.data.repository.OwnedAircraftRepository
import com.skytycoon.app.domain.model.AcquisitionType
import com.skytycoon.app.domain.model.AircraftCategory
import com.skytycoon.app.domain.model.AircraftModel
import com.skytycoon.app.domain.model.GameState
import com.skytycoon.app.domain.model.OwnedAircraft
import com.skytycoon.app.domain.model.UseCaseResult
import com.skytycoon.app.domain.usecase.PurchaseAircraftUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FleetUiState(
    val ownedAircraft: List<OwnedAircraft> = emptyList(),
    val availableModels: List<AircraftModel> = emptyList(),
    val gameState: GameState? = null,
    val filterCategory: AircraftCategory? = null,
    val showPurchaseDialog: Boolean = false,
    val selectedModelForPurchase: AircraftModel? = null,
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val successMsg: String? = null
)

@HiltViewModel
class FleetViewModel @Inject constructor(
    private val ownedAircraftRepository: OwnedAircraftRepository,
    private val aircraftModelRepository: AircraftModelRepository,
    private val purchaseAircraftUseCase: PurchaseAircraftUseCase,
    private val gameStateRepository: GameStateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FleetUiState())
    val uiState: StateFlow<FleetUiState> = _uiState.asStateFlow()

    val filteredAircraft: StateFlow<List<OwnedAircraft>> = _uiState
        .map { state ->
            if (state.filterCategory != null) {
                state.ownedAircraft.filter { it.model.category == state.filterCategory }
            } else {
                state.ownedAircraft
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    init {
        viewModelScope.launch {
            gameStateRepository.get().collect { gs ->
                _uiState.update { it.copy(gameState = gs) }
            }
        }
        viewModelScope.launch {
            ownedAircraftRepository.getAll().collect { aircraft ->
                _uiState.update { it.copy(ownedAircraft = aircraft) }
            }
        }
        viewModelScope.launch {
            aircraftModelRepository.getAll().collect { models ->
                _uiState.update { it.copy(availableModels = models) }
            }
        }
    }

    fun onFilterChange(category: AircraftCategory?) {
        _uiState.update { it.copy(filterCategory = category) }
    }

    fun onShowPurchaseDialog(model: AircraftModel) {
        _uiState.update { it.copy(showPurchaseDialog = true, selectedModelForPurchase = model) }
    }

    fun onDismissDialog() {
        _uiState.update { it.copy(showPurchaseDialog = false, selectedModelForPurchase = null) }
    }

    fun onPurchase(acquisitionType: AcquisitionType, registrationCode: String) {
        val model = _uiState.value.selectedModelForPurchase ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = purchaseAircraftUseCase(model.id, acquisitionType, registrationCode)) {
                is UseCaseResult.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        showPurchaseDialog = false,
                        selectedModelForPurchase = null,
                        successMsg = "Aircraft $registrationCode acquired!"
                    )
                }
                is UseCaseResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, errorMsg = result.message)
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMsg = null, successMsg = null) }
    }
}
