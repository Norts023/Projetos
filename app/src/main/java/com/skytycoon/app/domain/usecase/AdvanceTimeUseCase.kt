package com.skytycoon.app.domain.usecase

import com.skytycoon.app.config.GameBalanceConfig
import com.skytycoon.app.data.repository.ContractRepository
import com.skytycoon.app.data.repository.EmployeeRepository
import com.skytycoon.app.data.repository.FlightRepository
import com.skytycoon.app.data.repository.GameStateRepository
import com.skytycoon.app.data.repository.MissionRepository
import com.skytycoon.app.data.repository.OwnedAircraftRepository
import com.skytycoon.app.domain.model.AcquisitionType
import com.skytycoon.app.domain.model.Flight
import com.skytycoon.app.domain.model.FlightStatus
import com.skytycoon.app.domain.model.GameMode
import com.skytycoon.app.domain.model.GameState
import com.skytycoon.app.domain.model.Mission
import com.skytycoon.app.domain.model.MissionStatus
import com.skytycoon.app.domain.model.MissionType
import com.skytycoon.app.domain.model.OperationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AdvanceTimeResult(
    val newGameState: GameState,
    val completedFlights: List<Flight>,
    val missedEvents: List<String>
)

class AdvanceTimeUseCase @Inject constructor(
    private val gameStateRepository: GameStateRepository,
    private val flightRepository: FlightRepository,
    private val ownedAircraftRepository: OwnedAircraftRepository,
    private val employeeRepository: EmployeeRepository,
    private val missionRepository: MissionRepository,
    private val contractRepository: ContractRepository
) {
    // Primary entry point: accepts arbitrary deltaMinutes for auto-time loop
    suspend operator fun invoke(deltaMinutes: Int = GameBalanceConfig.ADVANCE_STEP_MINUTES.toInt()): AdvanceTimeResult =
        withContext(Dispatchers.IO) {
            val state = gameStateRepository.get().first()
                ?: return@withContext AdvanceTimeResult(
                    newGameState = GameState(companyName = "", gameMode = GameMode.FICTIONAL,
                        balanceCoins = 0L, reputation = 0, researchPoints = 0,
                        currentGameMinutes = 0L, dayNumber = 1),
                    completedFlights = emptyList(),
                    missedEvents = listOf("No active game state found")
                )

            val step = deltaMinutes.toLong()
            val oldMinutes = state.currentGameMinutes
            val newMinutes = oldMinutes + step

            val daysPassed = ((newMinutes / 1440L) - (oldMinutes / 1440L)).toInt()

            var balance = state.balanceCoins
            var uncollectedProfit = state.uncollectedProfitCoins
            var reputation = state.reputation
            var researchPoints = state.researchPoints
            var dayNumber = state.dayNumber
            val missedEvents = mutableListOf<String>()
            val completedFlights = mutableListOf<Flight>()

            if (daysPassed > 0) {
                dayNumber += daysPassed
                val employees = employeeRepository.getAll().first()
                val totalSalary = employees.sumOf { it.dailySalaryCoins } * daysPassed
                if (balance < totalSalary) missedEvents.add("Saldo insuficiente para salários ($totalSalary necessário)")
                balance = maxOf(0L, balance - totalSalary)
                researchPoints += GameBalanceConfig.RESEARCH_POINTS_PER_GAME_DAY * daysPassed
                val ownedAircraft = ownedAircraftRepository.getAll().first()
                val leasingCost = ownedAircraft
                    .filter { it.acquisitionType == AcquisitionType.LEASED }
                    .sumOf { it.model.leasingCostPerHourCoins * 24L * daysPassed }
                if (balance < leasingCost) missedEvents.add("Saldo insuficiente para arrendamento ($leasingCost necessário)")
                balance = maxOf(0L, balance - leasingCost)
                missionRepository.deleteExpiredDailies(dayNumber)
                buildDailyMissions(dayNumber).forEach { missionRepository.insert(it) }
            }

            val scheduledFlights = flightRepository.getByStatus(FlightStatus.SCHEDULED).first()
            scheduledFlights.filter { it.departureGameMinutes <= newMinutes }.forEach { flight ->
                flightRepository.update(flight.copy(status = FlightStatus.IN_FLIGHT))
                flight.assignedPilotId?.let { id ->
                    employeeRepository.getById(id)?.takeIf { it.currentFlightId == null }
                        ?.let { employeeRepository.update(it.copy(currentFlightId = flight.id)) }
                }
                flight.assignedCopilotId?.let { id ->
                    employeeRepository.getById(id)?.takeIf { it.currentFlightId == null }
                        ?.let { employeeRepository.update(it.copy(currentFlightId = flight.id)) }
                }
            }

            val inFlightFlights = flightRepository.getByStatus(FlightStatus.IN_FLIGHT).first()
            inFlightFlights.filter { it.arrivalGameMinutes <= newMinutes }.forEach { flight ->
                val completed = flight.copy(status = FlightStatus.COMPLETED)
                flightRepository.update(completed)
                completedFlights.add(completed)

                // Accumulate as uncollected profit — player collects via CollectProfitUseCase
                uncollectedProfit += flight.revenueCoins
                reputation = minOf(GameBalanceConfig.REPUTATION_MAX, reputation + GameBalanceConfig.REPUTATION_ON_TIME_BONUS)

                ownedAircraftRepository.getById(flight.aircraftId)?.let { aircraft ->
                    val dh = flight.durationHours
                    val newTotal = aircraft.totalFlightHours + dh
                    val condLoss = (GameBalanceConfig.CONDITION_LOSS_PER_FLIGHT_HOUR * dh).toInt()
                    val newMaint = if (newTotal >= aircraft.nextMaintenanceHours)
                        newTotal + GameBalanceConfig.MAINTENANCE_INTERVAL_HOURS else aircraft.nextMaintenanceHours
                    ownedAircraftRepository.update(aircraft.copy(
                        condition = maxOf(0, aircraft.condition - condLoss),
                        totalFlightHours = newTotal, nextMaintenanceHours = newMaint))
                }

                val restMinutes = newMinutes - flight.arrivalGameMinutes
                listOfNotNull(flight.assignedPilotId, flight.assignedCopilotId).forEach { crewId ->
                    employeeRepository.getById(crewId)?.let { crew ->
                        val fatGained = (GameBalanceConfig.FATIGUE_PER_FLIGHT_HOUR * flight.durationHours).toInt()
                        val recovered = (GameBalanceConfig.FATIGUE_RECOVERY_PER_GAME_HOUR * (restMinutes / 60L)).toInt()
                        employeeRepository.update(crew.copy(currentFlightId = null,
                            fatigue = (crew.fatigue + fatGained - recovered).coerceIn(0, 100)))
                    }
                }
            }

            contractRepository.expireOld(newMinutes)

            val updatedState = state.copy(
                currentGameMinutes = newMinutes, dayNumber = dayNumber,
                balanceCoins = balance, uncollectedProfitCoins = uncollectedProfit,
                reputation = reputation, researchPoints = researchPoints
            )
            gameStateRepository.update(updatedState)
            AdvanceTimeResult(newGameState = updatedState, completedFlights = completedFlights, missedEvents = missedEvents)
        }

    // Backward-compat overload for existing callers
    suspend fun invoke(fastMode: Boolean): AdvanceTimeResult =
        invoke(if (fastMode) GameBalanceConfig.FAST_ADVANCE_STEP_MINUTES.toInt() else GameBalanceConfig.ADVANCE_STEP_MINUTES.toInt())

    private fun buildDailyMissions(dayNumber: Int): List<Mission> = listOf(
        Mission(id = 0L, type = MissionType.DAILY, title = "Fretamento Diário",
            description = "Conclua 2 contratos de fretamento hoje", rewardMoneyCoins = 2_000L,
            rewardReputation = 2, rewardResearchPoints = 3, targetValue = 2L, currentValue = 0L,
            status = MissionStatus.ACTIVE, operationType = OperationType.CHARTER, expiresAtGameDay = dayNumber),
        Mission(id = 0L, type = MissionType.DAILY, title = "Dia de Helicóptero",
            description = "Conclua 1 operação de helicóptero hoje", rewardMoneyCoins = 1_500L,
            rewardReputation = 1, rewardResearchPoints = 2, targetValue = 1L, currentValue = 0L,
            status = MissionStatus.ACTIVE, operationType = OperationType.HELICOPTER, expiresAtGameDay = dayNumber),
        Mission(id = 0L, type = MissionType.DAILY, title = "Pontualidade do Dia",
            description = "Conclua 1 voo no horário hoje", rewardMoneyCoins = 1_000L,
            rewardReputation = 1, rewardResearchPoints = 1, targetValue = 1L, currentValue = 0L,
            status = MissionStatus.ACTIVE, operationType = null, expiresAtGameDay = dayNumber)
    )
}
