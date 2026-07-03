package com.skytycoon.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytycoon.app.config.GameBalanceConfig
import com.skytycoon.app.data.repository.ContractRepository
import com.skytycoon.app.data.repository.FlightRepository
import com.skytycoon.app.data.repository.GameStateRepository
import com.skytycoon.app.data.repository.OwnedAircraftRepository
import com.skytycoon.app.domain.model.GameState
import com.skytycoon.app.domain.usecase.AdvanceTimeUseCase
import com.skytycoon.app.domain.usecase.CollectProfitUseCase
import com.skytycoon.app.domain.usecase.GenerateContractsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val gameState: GameState? = null,
    val fleetSize: Int = 0,
    val activeFlightsCount: Int = 0,
    val availableContractsCount: Int = 0,
    val alerts: List<String> = emptyList(),
    val isAdvancing: Boolean = false,
    val uncollectedProfitCoins: Long = 0L,
    val speedMultiplier: Int = 1,
    val isAutoRunning: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val gameStateRepository: GameStateRepository,
    private val ownedAircraftRepository: OwnedAircraftRepository,
    private val flightRepository: FlightRepository,
    private val contractRepository: ContractRepository,
    private val advanceTimeUseCase: AdvanceTimeUseCase,
    private val generateContractsUseCase: GenerateContractsUseCase,
    private val collectProfitUseCase: CollectProfitUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var autoTimeJob: Job? = null

    init {
        combine(
            gameStateRepository.get(),
            ownedAircraftRepository.getAll(),
            flightRepository.getAll(),
            contractRepository.getAvailable()
        ) { gs, aircraft, flights, contracts ->
            val activeFlights = flights.filter { it.isActive }
            val maintenanceAlerts = aircraft
                .filter { it.needsMaintenance }
                .map { "${it.registrationCode} needs maintenance" }
            _uiState.value.copy(
                gameState = gs,
                fleetSize = aircraft.size,
                activeFlightsCount = activeFlights.size,
                availableContractsCount = contracts.size,
                alerts = maintenanceAlerts,
                uncollectedProfitCoins = gs?.uncollectedProfitCoins ?: 0L
            )
        }.onEach { newState ->
            _uiState.value = newState
        }.launchIn(viewModelScope)
    }

    fun onAdvanceTime(fast: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAdvancing = true) }
            advanceTimeUseCase(if (fast) GameBalanceConfig.FAST_ADVANCE_STEP_MINUTES.toInt() else GameBalanceConfig.ADVANCE_STEP_MINUTES.toInt())
            _uiState.update { it.copy(isAdvancing = false) }
        }
    }

    fun onToggleAutoTime() {
        if (_uiState.value.isAutoRunning) {
            autoTimeJob?.cancel()
            autoTimeJob = null
            _uiState.update { it.copy(isAutoRunning = false) }
        } else {
            _uiState.update { it.copy(isAutoRunning = true) }
            autoTimeJob = viewModelScope.launch {
                while (isActive) {
                    val deltaMinutes = (_uiState.value.speedMultiplier * GameBalanceConfig.ADVANCE_STEP_MINUTES).toInt()
                    advanceTimeUseCase(deltaMinutes)
                    delay(1000L)
                }
            }
        }
    }

    fun onSetSpeed(multiplier: Int) {
        val wasAutoRunning = _uiState.value.isAutoRunning
        _uiState.update { it.copy(speedMultiplier = multiplier) }
        if (wasAutoRunning) {
            autoTimeJob?.cancel()
            autoTimeJob = null
            autoTimeJob = viewModelScope.launch {
                while (isActive) {
                    val deltaMinutes = (_uiState.value.speedMultiplier * GameBalanceConfig.ADVANCE_STEP_MINUTES).toInt()
                    advanceTimeUseCase(deltaMinutes)
                    delay(1000L)
                }
            }
        }
    }

    fun onCollectProfit() {
        viewModelScope.launch {
            collectProfitUseCase()
        }
    }

    fun onGenerateContracts() {
        viewModelScope.launch {
            generateContractsUseCase()
        }
    }
}
