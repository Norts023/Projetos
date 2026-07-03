package com.skytycoon.app.ui.screens.purchaseaircraft

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytycoon.app.data.repository.AircraftModelRepository
import com.skytycoon.app.data.repository.GameStateRepository
import com.skytycoon.app.domain.model.AcquisitionType
import com.skytycoon.app.domain.model.AircraftModel
import com.skytycoon.app.domain.model.UseCaseResult
import com.skytycoon.app.domain.usecase.PurchaseAircraftUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PurchaseAircraftUiState(
    val models: List<AircraftModel> = emptyList(),
    val selectedManufacturer: String? = null,
    val selectedModel: AircraftModel? = null,
    val message: String? = null,
    val gameBalance: Long = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class PurchaseAircraftViewModel @Inject constructor(
    private val aircraftModelRepository: AircraftModelRepository,
    private val gameStateRepository: GameStateRepository,
    private val purchaseAircraftUseCase: PurchaseAircraftUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseAircraftUiState())
    val uiState: StateFlow<PurchaseAircraftUiState> = _uiState.asStateFlow()

    /** Distinct manufacturer names derived from the loaded models list. */
    val manufacturers: StateFlow<List<String>> = _uiState
        .map { state -> state.models.map { it.manufacturer }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    init {
        aircraftModelRepository.getAll()
            .onEach { models ->
                _uiState.update { state ->
                    // Pre-select first manufacturer if none is set
                    val firstManufacturer = models.map { it.manufacturer }.distinct().sorted()
                        .firstOrNull()
                    state.copy(
                        models = models,
                        selectedManufacturer = state.selectedManufacturer ?: firstManufacturer
                    )
                }
            }
            .launchIn(viewModelScope)

        gameStateRepository.get()
            .onEach { gameState ->
                _uiState.update { it.copy(gameBalance = gameState?.balanceCoins ?: 0L) }
            }
            .launchIn(viewModelScope)
    }

    fun onSelectManufacturer(manufacturer: String) {
        _uiState.update {
            it.copy(
                selectedManufacturer = manufacturer,
                selectedModel = null // reset model selection on tab change
            )
        }
    }

    fun onSelectModel(model: AircraftModel) {
        _uiState.update {
            // Toggle deselection: tapping the already-selected model dismisses the sheet
            val newSelection = if (it.selectedModel?.id == model.id) null else model
            it.copy(selectedModel = newSelection)
        }
    }

    fun onDismissModelSheet() {
        _uiState.update { it.copy(selectedModel = null) }
    }

    /**
     * Executes the purchase for [uiState.selectedModel].
     * Defaults to [AcquisitionType.PURCHASED] and auto-generates a registration code.
     */
    fun onPurchase() {
        val model = _uiState.value.selectedModel ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val registrationCode = generateRegistrationCode(model)
            when (val result = purchaseAircraftUseCase(
                modelId = model.id,
                acquisitionType = AcquisitionType.PURCHASED,
                registrationCode = registrationCode
            )) {
                is UseCaseResult.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedModel = null,
                        message = "Aeronave $registrationCode adquirida com sucesso!"
                    )
                }
                is UseCaseResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, message = result.message)
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun generateRegistrationCode(model: AircraftModel): String {
        val prefix = model.manufacturer.take(2).uppercase()
        val suffix = model.model.filter { it.isLetterOrDigit() }.take(3).uppercase()
        val number = (1000..9999).random()
        return "$prefix-$suffix$number"
    }
}
