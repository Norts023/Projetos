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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScheduleUiState(
    val flights: List<Flight> = emptyList(),
    val contracts: List<Contract> = emptyList(),
    val airports: List<Airport> = emptyList(),
    val ownedAircraft: List<OwnedAircraft> = emptyList(),
    val employees: List<Employee> = emptyList(),
    val gameState: GameState? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showScheduleSheet: Boolean = false
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val flightRepository: FlightRepository,
    private val contractRepository: ContractRepository,
    private val airportRepository: AirportRepository,
    private val ownedAircraftRepository: OwnedAircraftRepository,
    private val employeeRepository: EmployeeRepository,
    private val gameStateRepository: GameStateRepository,
    private val scheduleFlightUseCase: ScheduleFlightUseCase
) : ViewModel() {

    private data class ExtraState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val showScheduleSheet: Boolean = false
    )

    private val _extraState = MutableStateFlow(ExtraState())

    val uiState: StateFlow<ScheduleUiState> = combine(
        combine(
            flightRepository.getAll(),
            contractRepository.getAvailable(),
            airportRepository.getAll()
        ) { flights, contracts, airports ->
            Triple(flights, contracts, airports)
        },
        combine(
            ownedAircraftRepository.getAll(),
            employeeRepository.getAll(),
            gameStateRepository.get()
        ) { aircraft, employees, gameState ->
            Triple(aircraft, employees, gameState)
        },
        _extraState
    ) { dataA, dataB, extra ->
        val (flights, contracts, airports) = dataA
        val (aircraft, employees, gameState) = dataB
        ScheduleUiState(
            flights = flights,
            contracts = contracts,
            airports = airports,
            ownedAircraft = aircraft,
            employees = employees,
            gameState = gameState as GameState?,
            isLoading = extra.isLoading,
            errorMessage = extra.errorMessage,
            showScheduleSheet = extra.showScheduleSheet
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ScheduleUiState(isLoading = true)
    )

    fun onScheduleFlight(request: ScheduleFlightRequest) {
        viewModelScope.launch {
            _extraState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = scheduleFlightUseCase.invoke(request)) {
                is UseCaseResult.Success -> {
                    _extraState.update { it.copy(isLoading = false, showScheduleSheet = false) }
                }
                is UseCaseResult.Failure -> {
                    _extraState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun onShowScheduleSheet(show: Boolean) {
        _extraState.update { it.copy(showScheduleSheet = show) }
    }

    fun onDismissError() {
        _extraState.update { it.copy(errorMessage = null) }
    }
}
