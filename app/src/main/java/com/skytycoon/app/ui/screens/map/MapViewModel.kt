package com.skytycoon.app.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytycoon.app.data.repository.AirportRepository
import com.skytycoon.app.data.repository.FlightRepository
import com.skytycoon.app.domain.model.Airport
import com.skytycoon.app.domain.model.Flight
import com.skytycoon.app.domain.model.MapSettings
import com.skytycoon.app.domain.usecase.GetMapSettingsUseCase
import com.skytycoon.app.domain.usecase.UpdateMapSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val airports: List<Airport> = emptyList(),
    val activeFlights: List<Flight> = emptyList(),
    val selectedAirport: Airport? = null,
    val mapSettings: MapSettings = MapSettings(),
    val showSettingsSheet: Boolean = false
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val airportRepository: AirportRepository,
    private val flightRepository: FlightRepository,
    private val getMapSettingsUseCase: GetMapSettingsUseCase,
    private val updateMapSettingsUseCase: UpdateMapSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        airportRepository.getAll()
            .onEach { airports -> _uiState.update { it.copy(airports = airports) } }
            .launchIn(viewModelScope)

        flightRepository.getAll()
            .onEach { flights ->
                _uiState.update { it.copy(activeFlights = flights.filter { f -> f.isActive }) }
            }
            .launchIn(viewModelScope)

        getMapSettingsUseCase()
            .onEach { settings -> _uiState.update { it.copy(mapSettings = settings) } }
            .launchIn(viewModelScope)
    }

    fun onAirportSelected(airport: Airport?) {
        _uiState.update { it.copy(selectedAirport = airport) }
    }

    fun onToggleSettingsSheet() {
        _uiState.update { it.copy(showSettingsSheet = !it.showSettingsSheet) }
    }

    fun onUpdateSettings(settings: MapSettings) {
        viewModelScope.launch {
            updateMapSettingsUseCase(settings)
        }
    }
}
