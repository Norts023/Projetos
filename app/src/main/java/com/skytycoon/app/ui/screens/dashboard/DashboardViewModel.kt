package com.skytycoon.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytycoon.app.data.repository.AirportRepository
import com.skytycoon.app.data.repository.ContractRepository
import com.skytycoon.app.data.repository.FlightRepository
import com.skytycoon.app.data.repository.GameStateRepository
import com.skytycoon.app.data.repository.OwnedAircraftRepository
import com.skytycoon.app.domain.model.Airport
import com.skytycoon.app.domain.model.FlightStatus
import com.skytycoon.app.domain.model.GameState
import com.skytycoon.app.domain.usecase.AdvanceTimeUseCase
import com.skytycoon.app.domain.usecase.GenerateContractsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val gameState: GameState? = null,
    val fleetSize: Int = 0,
    val activeFlightsCount: Int = 0,
    val availableContractsCount: Int = 0,
    val alerts: List<String> = emptyList(),
    val isAdvancingTime: Boolean = false,
    val airports: List<Airport> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val gameStateRepository: GameStateRepository,
    private val ownedAircraftRepository: OwnedAircraftRepository,
    private val flightRepository: FlightRepository,
    private val contractRepository: ContractRepository,
    private val airportRepository: AirportRepository,
    private val advanceTimeUseCase: AdvanceTimeUseCase,
    private val generateContractsUseCase: GenerateContractsUseCase
) : ViewModel() {

    private val _isAdvancingTime = MutableStateFlow(false)

    val uiState: StateFlow<DashboardUiState> = combine(
        gameStateRepository.get(),
        ownedAircraftRepository.getAll(),
        flightRepository.getAll(),
        contractRepository.getAvailable(),
        airportRepository.getAll(),
        _isAdvancingTime
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val gameState = values[0] as GameState?
        @Suppress("UNCHECKED_CAST")
        val ownedAircraft = values[1] as List<com.skytycoon.app.domain.model.OwnedAircraft>
        @Suppress("UNCHECKED_CAST")
        val flights = values[2] as List<com.skytycoon.app.domain.model.Flight>
        @Suppress("UNCHECKED_CAST")
        val contracts = values[3] as List<com.skytycoon.app.domain.model.Contract>
        @Suppress("UNCHECKED_CAST")
        val airports = values[4] as List<Airport>
        val isAdvancingTime = values[5] as Boolean

        val activeFlights = flights.count { flight ->
            flight.status in listOf(
                FlightStatus.SCHEDULED,
                FlightStatus.IN_FLIGHT,
                FlightStatus.BOARDING
            )
        }

        val maintenanceCount = ownedAircraft.count { it.needsMaintenance }
        val alerts = buildList {
            if (maintenanceCount > 0) {
                add("$maintenanceCount aircraft need maintenance")
            }
        }

        DashboardUiState(
            gameState = gameState,
            fleetSize = ownedAircraft.size,
            activeFlightsCount = activeFlights,
            availableContractsCount = contracts.size,
            alerts = alerts,
            isAdvancingTime = isAdvancingTime,
            airports = airports
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun onAdvanceTime(fastMode: Boolean) {
        viewModelScope.launch {
            _isAdvancingTime.value = true
            try {
                advanceTimeUseCase.invoke(fastMode)
            } finally {
                _isAdvancingTime.value = false
            }
        }
    }

    fun onGenerateContracts() {
        viewModelScope.launch {
            generateContractsUseCase.invoke()
        }
    }
}
