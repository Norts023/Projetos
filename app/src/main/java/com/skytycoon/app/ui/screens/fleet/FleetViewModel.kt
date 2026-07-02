package com.skytycoon.app.ui.screens.fleet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytycoon.app.data.repository.AircraftModelRepository
import com.skytycoon.app.data.repository.OwnedAircraftRepository
import com.skytycoon.app.domain.model.AcquisitionType
import com.skytycoon.app.domain.model.AircraftCategory
import com.skytycoon.app.domain.model.AircraftModel
import com.skytycoon.app.domain.model.OwnedAircraft
import com.skytycoon.app.domain.model.UseCaseResult
import com.skytycoon.app.domain.usecase.PurchaseAircraftUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FleetUiState(
    val ownedAircraft: List<OwnedAircraft> = emptyList(),
    val aircraftModels: List<AircraftModel> = emptyList(),
    val filterCategory: AircraftCategory? = null,
    val isLoading: Boolean = false,
    val showPurchaseDialog: Boolean = false,
    val purchaseError: String? = null
) {
    val filteredAircraft: List<OwnedAircraft>
        get() = if (filterCategory != null) {
            ownedAircraft.filter { it.model.category == filterCategory }
        } else {
            ownedAircraft
        }
}

@HiltViewModel
class FleetViewModel @Inject constructor(
    private val ownedAircraftRepository: OwnedAircraftRepository,
    private val aircraftModelRepository: AircraftModelRepository,
    private val purchaseAircraftUseCase: PurchaseAircraftUseCase
) : ViewModel() {

    private val _filterCategory = MutableStateFlow<AircraftCategory?>(null)
    private val _showPurchaseDialog = MutableStateFlow(false)
    private val _purchaseError = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<FleetUiState> = combine(
        ownedAircraftRepository.getAll(),
        aircraftModelRepository.getAll(),
        _filterCategory,
        _showPurchaseDialog,
        _purchaseError,
        _isLoading
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val ownedAircraft = values[0] as List<OwnedAircraft>
        @Suppress("UNCHECKED_CAST")
        val aircraftModels = values[1] as List<AircraftModel>
        val filterCategory = values[2] as AircraftCategory?
        val showPurchaseDialog = values[3] as Boolean
        val purchaseError = values[4] as String?
        val isLoading = values[5] as Boolean

        FleetUiState(
            ownedAircraft = ownedAircraft,
            aircraftModels = aircraftModels,
            filterCategory = filterCategory,
            isLoading = isLoading,
            showPurchaseDialog = showPurchaseDialog,
            purchaseError = purchaseError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FleetUiState()
    )

    fun onFilterChange(category: AircraftCategory?) {
        _filterCategory.value = category
    }

    fun onShowPurchaseDialog(show: Boolean) {
        _showPurchaseDialog.value = show
        if (!show) {
            _purchaseError.value = null
        }
    }

    fun onPurchase(modelId: Int, type: AcquisitionType, regCode: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _purchaseError.value = null
            try {
                when (val result = purchaseAircraftUseCase.invoke(modelId, type, regCode)) {
                    is UseCaseResult.Success -> {
                        _showPurchaseDialog.value = false
                    }
                    is UseCaseResult.Failure -> {
                        _purchaseError.value = result.message
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
