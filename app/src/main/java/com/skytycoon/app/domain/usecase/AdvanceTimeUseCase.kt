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
    suspend operator fun invoke(fastMode: Boolean = false): AdvanceTimeResult =
        withContext(Dispatchers.IO) {
            val state = gameStateRepository.get().first()
                ?: return@withContext AdvanceTimeResult(
                    newGameState = GameState(
                        companyName = "",
                        gameMode = com.skytycoon.app.domain.model.GameMode.FICTIONAL,
                        balanceCoins = 0L,
                        reputation = 0,
                        researchPoints = 0,
                        currentGameMinutes = 0L,
                        dayNumber = 1
                    ),
                    completedFlights = emptyList(),
                    missedEvents = listOf("No active game state found")
                )

            val step = if (fastMode) {
                GameBalanceConfig.FAST_ADVANCE_STEP_MINUTES
            } else {
                GameBalanceConfig.ADVANCE_STEP_MINUTES
            }

            val oldMinutes = state.currentGameMinutes
            val newMinutes = oldMinutes + step

            val oldDayBoundary = oldMinutes / 1440L
            val newDayBoundary = newMinutes / 1440L
            val daysPassed = (newDayBoundary - oldDayBoundary).toInt()

            var balance = state.balanceCoins
            var reputation = state.reputation
            var researchPoints = state.researchPoints
            var dayNumber = state.dayNumber
            val missedEvents = mutableListOf<String>()
            val completedFlights = mutableListOf<Flight>()

            // --- Day change processing ---
            if (daysPassed > 0) {
                dayNumber += daysPassed

                // Pay employee salaries
                val employees = employeeRepository.getAll().first()
                val totalSalary = employees.sumOf { it.dailySalaryCoins } * daysPassed
                if (balance < totalSalary) {
                    missedEvents.add("Insufficient funds for employee salaries (needed $totalSalary, had $balance)")
                }
                balance = maxOf(0L, balance - totalSalary)

                // Add research points
                researchPoints += GameBalanceConfig.RESEARCH_POINTS_PER_GAME_DAY * daysPassed

                // Pay leasing costs for leased aircraft (24h/day per leased aircraft)
                val ownedAircraft = ownedAircraftRepository.getAll().first()
                val leasingCost = ownedAircraft
                    .filter { it.acquisitionType == AcquisitionType.LEASED }
                    .sumOf { it.model.leasingCostPerHourCoins * 24L * daysPassed }
                if (balance < leasingCost) {
                    missedEvents.add("Insufficient funds for leasing costs (needed $leasingCost)")
                }
                balance = maxOf(0L, balance - leasingCost)

                // Delete expired daily missions and generate new ones for each new day
                missionRepository.deleteExpiredDailies(dayNumber)
                repeat(daysPassed) { offset ->
                    buildDailyMissions(dayNumber - daysPassed + offset + 1)
                        .forEach { missionRepository.insert(it) }
                }
            }

            // --- Update SCHEDULED → IN_FLIGHT ---
            val scheduledFlights = flightRepository.getByStatus(FlightStatus.SCHEDULED).first()
            scheduledFlights
                .filter { it.departureGameMinutes <= newMinutes }
                .forEach { flight ->
                    flightRepository.update(flight.copy(status = FlightStatus.IN_FLIGHT))

                    // Mark pilot as assigned to this flight (fatigue will accumulate on completion)
                    flight.assignedPilotId?.let { pilotId ->
                        val pilot = employeeRepository.getById(pilotId)
                        if (pilot != null && pilot.currentFlightId == null) {
                            employeeRepository.update(pilot.copy(currentFlightId = flight.id))
                        }
                    }
                    flight.assignedCopilotId?.let { copilotId ->
                        val copilot = employeeRepository.getById(copilotId)
                        if (copilot != null && copilot.currentFlightId == null) {
                            employeeRepository.update(copilot.copy(currentFlightId = flight.id))
                        }
                    }
                }

            // --- Complete IN_FLIGHT flights ---
            val inFlightFlights = flightRepository.getByStatus(FlightStatus.IN_FLIGHT).first()
            inFlightFlights
                .filter { it.arrivalGameMinutes <= newMinutes }
                .forEach { flight ->
                    val completed = flight.copy(status = FlightStatus.COMPLETED)
                    flightRepository.update(completed)
                    completedFlights.add(completed)

                    // Credit revenue and reputation
                    balance += flight.revenueCoins
                    reputation = minOf(
                        GameBalanceConfig.REPUTATION_MAX,
                        reputation + GameBalanceConfig.REPUTATION_ON_TIME_BONUS
                    )

                    // Update aircraft condition and total hours
                    val aircraft = ownedAircraftRepository.getById(flight.aircraftId)
                    if (aircraft != null) {
                        val durationHours = flight.durationHours
                        val newTotalHours = aircraft.totalFlightHours + durationHours
                        val conditionLoss = (GameBalanceConfig.CONDITION_LOSS_PER_FLIGHT_HOUR * durationHours).toInt()
                        val newNextMaint = if (newTotalHours >= aircraft.nextMaintenanceHours) {
                            newTotalHours + GameBalanceConfig.MAINTENANCE_INTERVAL_HOURS
                        } else {
                            aircraft.nextMaintenanceHours
                        }
                        ownedAircraftRepository.update(
                            aircraft.copy(
                                condition = maxOf(0, aircraft.condition - conditionLoss),
                                totalFlightHours = newTotalHours,
                                nextMaintenanceHours = newNextMaint
                            )
                        )
                    }

                    // Recover pilot fatigue for newly free pilots
                    val gameHoursElapsed = step / 60L
                    listOfNotNull(flight.assignedPilotId, flight.assignedCopilotId).forEach { crewId ->
                        val crew = employeeRepository.getById(crewId)
                        if (crew != null) {
                            val flightFatigue = (GameBalanceConfig.FATIGUE_PER_FLIGHT_HOUR * flight.durationHours).toInt()
                            val recovery = (GameBalanceConfig.FATIGUE_RECOVERY_PER_GAME_HOUR * gameHoursElapsed).toInt()
                            val newFatigue = (crew.fatigue + flightFatigue - recovery).coerceIn(0, 100)
                            employeeRepository.update(
                                crew.copy(
                                    currentFlightId = null,
                                    fatigue = newFatigue
                                )
                            )
                        }
                    }
                }

            // --- Expire old contracts ---
            contractRepository.expireOld(newMinutes)

            // --- Save updated game state ---
            val updatedState = state.copy(
                currentGameMinutes = newMinutes,
                dayNumber = dayNumber,
                balanceCoins = balance,
                reputation = reputation,
                researchPoints = researchPoints
            )
            gameStateRepository.update(updatedState)

            AdvanceTimeResult(
                newGameState = updatedState,
                completedFlights = completedFlights,
                missedEvents = missedEvents
            )
        }

    private fun buildDailyMissions(dayNumber: Int): List<com.skytycoon.app.domain.model.Mission> = listOf(
        com.skytycoon.app.domain.model.Mission(
            id = 0L,
            type = com.skytycoon.app.domain.model.MissionType.DAILY,
            title = "Daily Charter",
            description = "Complete 2 charter contracts today",
            rewardMoneyCoins = 2_000L,
            rewardReputation = 2,
            rewardResearchPoints = 3,
            targetValue = 2L,
            currentValue = 0L,
            status = com.skytycoon.app.domain.model.MissionStatus.ACTIVE,
            operationType = com.skytycoon.app.domain.model.OperationType.CHARTER,
            expiresAtGameDay = dayNumber
        ),
        com.skytycoon.app.domain.model.Mission(
            id = 0L,
            type = com.skytycoon.app.domain.model.MissionType.DAILY,
            title = "Helicopter Day",
            description = "Complete 1 helicopter operation today",
            rewardMoneyCoins = 1_500L,
            rewardReputation = 1,
            rewardResearchPoints = 2,
            targetValue = 1L,
            currentValue = 0L,
            status = com.skytycoon.app.domain.model.MissionStatus.ACTIVE,
            operationType = com.skytycoon.app.domain.model.OperationType.HELICOPTER,
            expiresAtGameDay = dayNumber
        ),
        com.skytycoon.app.domain.model.Mission(
            id = 0L,
            type = com.skytycoon.app.domain.model.MissionType.DAILY,
            title = "Punctual Today",
            description = "Complete 1 on-time landing today",
            rewardMoneyCoins = 1_000L,
            rewardReputation = 1,
            rewardResearchPoints = 1,
            targetValue = 1L,
            currentValue = 0L,
            status = com.skytycoon.app.domain.model.MissionStatus.ACTIVE,
            operationType = null,
            expiresAtGameDay = dayNumber
        )
    )
}
