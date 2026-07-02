package com.skytycoon.app.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytycoon.app.data.repository.AirportRepository
import com.skytycoon.app.data.repository.ContractRepository
import com.skytycoon.app.data.repository.EmployeeRepository
import com.skytycoon.app.data.repository.FlightRepository
import com.skytycoon.app.data.repository.GameStateRepository
import com.skytycoon.app.data.repository.OwnedAircraftRepository
import com.skytycoon.app.domain.model.Airport
import com.skytycoon.app.domain.model.Contract
import com.skytycoon.app.domain.model.Employee
import com.skytycoon.app.domain.model.Flight
import com.skytycoon.app.domain.model.GameState
import com.skytycoon.app.domain.model.OwnedAircraft
import com.skytycoon.app.domain.model.UseCaseResult
import com.skytycoon.app.domain.usecase.ScheduleFlightRequest
import com.skytycoon.app.domain.usecase.ScheduleFlightUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScheduleUiState(
    val flights: List<Flight> = emptyList(),
    val contracts: List<Contract> = emptyList(),
    val ownedAircraft: List<OwnedAircraft> = emptyList(),
    val airports: List<Airport> = emptyList(),
    val availableEmployees: List<Employee> = emptyList(),
    val gameState: GameState? = null,
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val successMsg: String? = null
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val flightRepository: FlightRepository,
    private val contractRepository: ContractRepository,
    private val ownedAircraftRepository: OwnedAircraftRepository,
    private val employeeRepository: EmployeeRepository,
    private val airportRepository: AirportRepository,
    private val gameStateRepository: GameStateRepository,
    private val scheduleFlightUseCase: ScheduleFlightUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        combine(
            flightRepository.getAll(),
            contractRepository.getAvailable(),
            ownedAircraftRepository.getAll(),
            employeeRepository.getAll()
        ) { flights, contracts, aircraft, employees ->
            val availEmp = employees.filter { it.isAvailable }
            _uiState.update {
                it.copy(
                    flights = flights,
                    contracts = contracts,
                    ownedAircraft = aircraft,
                    availableEmployees = availEmp
                )
            }
        }.launchIn(viewModelScope)

        gameStateRepository.get()
            .onEach { gs -> _uiState.update { it.copy(gameState = gs) } }
            .launchIn(viewModelScope)

        airportRepository.getAll()
            .onEach { airports -> _uiState.update { it.copy(airports = airports) } }
            .launchIn(viewModelScope)
    }

    fun onScheduleFlight(request: ScheduleFlightRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = scheduleFlightUseCase(request)) {
                is UseCaseResult.Success -> _uiState.update {
                    it.copy(isLoading = false, successMsg = "Flight scheduled!")
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

    fun clearSuccessMessage() { _uiState.update { it.copy(successMsg = null) } }
    fun clearErrorMessage() { _uiState.update { it.copy(errorMsg = null) } }
}
