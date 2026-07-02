package com.skytycoon.app.domain.usecase

import com.skytycoon.app.data.repository.AircraftModelRepository
import com.skytycoon.app.data.repository.AirportRepository
import com.skytycoon.app.data.repository.ContractRepository
import com.skytycoon.app.data.repository.EmployeeRepository
import com.skytycoon.app.data.repository.FlightRepository
import com.skytycoon.app.data.repository.GameStateRepository
import com.skytycoon.app.data.repository.OwnedAircraftRepository
import com.skytycoon.app.domain.model.Contract
import com.skytycoon.app.domain.model.ContractStatus
import com.skytycoon.app.domain.model.Flight
import com.skytycoon.app.domain.model.FlightStatus
import com.skytycoon.app.domain.model.GameMode
import com.skytycoon.app.domain.model.OperationType
import com.skytycoon.app.domain.model.UseCaseResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ScheduleFlightRequest(
    val aircraftId: Long,
    val operationType: OperationType,
    val originIata: String,
    val destinationIata: String,
    val departureGameMinutes: Long,
    val ticketPricePerPax: Long? = null,
    val passengerCount: Int? = null,
    val contractId: Long? = null,
    val assignedPilotId: Long? = null,
    val assignedCopilotId: Long? = null
)

class ScheduleFlightUseCase @Inject constructor(
    private val ownedAircraftRepository: OwnedAircraftRepository,
    private val aircraftModelRepository: AircraftModelRepository,
    private val flightRepository: FlightRepository,
    private val airportRepository: AirportRepository,
    private val contractRepository: ContractRepository,
    private val gameStateRepository: GameStateRepository,
    private val employeeRepository: EmployeeRepository
) {
    suspend operator fun invoke(request: ScheduleFlightRequest): UseCaseResult<Flight> =
        withContext(Dispatchers.IO) {
            // 1. Validate aircraft exists and is operational
            val aircraft = ownedAircraftRepository.getById(request.aircraftId)
                ?: return@withContext UseCaseResult.Failure("Aircraft not found")

            if (!aircraft.isOperational) {
                return@withContext UseCaseResult.Failure(
                    "Aircraft is not operational (condition: ${aircraft.condition}%). " +
                    "Needs maintenance or condition too low."
                )
            }

            // 2. Resolve origin and destination airports
            val origin = airportRepository.getByIata(request.originIata)
                ?: return@withContext UseCaseResult.Failure("Origin airport '${request.originIata}' not found")

            val destination = airportRepository.getByIata(request.destinationIata)
                ?: return@withContext UseCaseResult.Failure("Destination airport '${request.destinationIata}' not found")

            if (origin.iata == destination.iata) {
                return@withContext UseCaseResult.Failure("Origin and destination airports must be different")
            }

            // 3. Check aircraft range
            val distanceKm = origin.distanceKmTo(destination)
            if (!aircraft.model.isCapableOfRoute(distanceKm)) {
                return@withContext UseCaseResult.Failure(
                    "Route distance (${distanceKm.toInt()} km) exceeds aircraft range (${aircraft.model.rangeKm} km)"
                )
            }

            // 4. Calculate arrival time
            val flightDurationMinutes = (distanceKm / aircraft.model.cruiseSpeedKmh * 60).toLong()
            val arrivalGameMinutes = request.departureGameMinutes + flightDurationMinutes

            // 5. Check for schedule conflicts
            val activeFlights = flightRepository.getActiveForAircraft(request.aircraftId)
            val hasConflict = activeFlights.any { existing ->
                existing.departureGameMinutes < arrivalGameMinutes &&
                existing.arrivalGameMinutes > request.departureGameMinutes
            }
            if (hasConflict) {
                return@withContext UseCaseResult.Failure(
                    "Schedule conflict: this aircraft already has an active flight during that time window"
                )
            }

            // 6. Pilot validation in REALISTIC mode
            val gameState = gameStateRepository.get().first()
            if (gameState?.gameMode == GameMode.REALISTIC) {
                val pilotId = request.assignedPilotId
                    ?: return@withContext UseCaseResult.Failure(
                        "A pilot must be assigned in Realistic mode"
                    )
                val pilot = employeeRepository.getById(pilotId)
                    ?: return@withContext UseCaseResult.Failure("Assigned pilot not found")
                if (!pilot.isAvailable) {
                    return@withContext UseCaseResult.Failure(
                        "Pilot '${pilot.name}' is not available (fatigued or already on a flight)"
                    )
                }
                if (!pilot.canFlyAircraftCategory(aircraft.model.category)) {
                    return@withContext UseCaseResult.Failure(
                        "Pilot '${pilot.name}' is not qualified to fly ${aircraft.model.category} aircraft"
                    )
                }
            }

            // 7. Calculate revenue
            val revenueCoins: Long = when (request.operationType) {
                OperationType.AIRLINE -> {
                    val pax = request.passengerCount
                        ?: return@withContext UseCaseResult.Failure("Passenger count is required for airline flights")
                    val price = request.ticketPricePerPax
                        ?: return@withContext UseCaseResult.Failure("Ticket price per passenger is required for airline flights")
                    pax * price
                }
                OperationType.CHARTER, OperationType.HELICOPTER -> {
                    val contractId = request.contractId
                        ?: return@withContext UseCaseResult.Failure(
                            "A contract ID is required for ${request.operationType.name} flights"
                        )
                    val contract = contractRepository.getById(contractId)
                        ?: return@withContext UseCaseResult.Failure("Contract not found")
                    if (contract.status != ContractStatus.AVAILABLE) {
                        return@withContext UseCaseResult.Failure("Contract is no longer available")
                    }
                    contract.totalValueCoins
                }
            }

            val passengerCount = when (request.operationType) {
                OperationType.AIRLINE -> request.passengerCount ?: 0
                OperationType.CHARTER, OperationType.HELICOPTER -> {
                    val contract = request.contractId?.let { contractRepository.getById(it) }
                    contract?.passengerCount ?: 0
                }
            }

            // 8. Insert flight
            val flight = Flight(
                id = 0L,
                operationType = request.operationType,
                aircraftId = request.aircraftId,
                originIata = request.originIata,
                destinationIata = request.destinationIata,
                departureGameMinutes = request.departureGameMinutes,
                arrivalGameMinutes = arrivalGameMinutes,
                passengerCount = passengerCount,
                revenueCoins = revenueCoins,
                status = FlightStatus.SCHEDULED,
                assignedPilotId = request.assignedPilotId,
                assignedCopilotId = request.assignedCopilotId,
                contractId = request.contractId
            )
            val newFlightId = flightRepository.insert(flight)
            val insertedFlight = flight.copy(id = newFlightId)

            // 9. Mark contract as ACCEPTED if one is attached
            request.contractId?.let { contractId ->
                val contract = contractRepository.getById(contractId)
                if (contract != null) {
                    contractRepository.update(
                        contract.copy(
                            status = ContractStatus.ACCEPTED,
                            assignedFlightId = newFlightId
                        )
                    )
                }
            }

            UseCaseResult.Success(insertedFlight)
        }
}
