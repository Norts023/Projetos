package com.skytycoon.app.domain.usecase

import android.content.Context
import com.skytycoon.app.data.repository.AircraftModelRepository
import com.skytycoon.app.data.repository.AirportRepository
import com.skytycoon.app.data.repository.ContractRepository
import com.skytycoon.app.data.repository.EmployeeRepository
import com.skytycoon.app.data.repository.FlightRepository
import com.skytycoon.app.data.repository.GameStateRepository
import com.skytycoon.app.data.repository.OwnedAircraftRepository
import com.skytycoon.app.domain.model.AcquisitionType
import com.skytycoon.app.domain.model.AircraftCategory
import com.skytycoon.app.domain.model.AircraftModel
import com.skytycoon.app.domain.model.Airport
import com.skytycoon.app.domain.model.Contract
import com.skytycoon.app.domain.model.ContractStatus
import com.skytycoon.app.domain.model.Employee
import com.skytycoon.app.domain.model.EmployeeType
import com.skytycoon.app.domain.model.Flight
import com.skytycoon.app.domain.model.FlightStatus
import com.skytycoon.app.domain.model.GameMode
import com.skytycoon.app.domain.model.GameState
import com.skytycoon.app.domain.model.OperationType
import com.skytycoon.app.domain.model.OwnedAircraft
import com.skytycoon.app.domain.model.UseCaseResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScheduleFlightUseCaseTest {

    // -----------------------------------------------------------------------
    // Fake repositories
    // -----------------------------------------------------------------------

    private class FakeOwnedAircraftRepository : OwnedAircraftRepository {
        val aircraft = mutableMapOf<Long, OwnedAircraft>()
        private var nextId = 1L

        override fun getAll(): Flow<List<OwnedAircraft>> = flowOf(aircraft.values.toList())
        override suspend fun getById(id: Long): OwnedAircraft? = aircraft[id]
        override suspend fun insert(a: OwnedAircraft): Long {
            val id = nextId++
            aircraft[id] = a.copy(id = id)
            return id
        }
        override suspend fun update(a: OwnedAircraft) { aircraft[a.id] = a }
        override suspend fun delete(id: Long) { aircraft.remove(id) }
    }

    private class FakeAircraftModelRepository : AircraftModelRepository {
        val models = mutableMapOf<Int, AircraftModel>()

        override fun getAll(): Flow<List<AircraftModel>> = flowOf(models.values.toList())
        override suspend fun getById(id: Int): AircraftModel? = models[id]
        override suspend fun seedIfEmpty(context: Context) {}
    }

    private class FakeFlightRepository : FlightRepository {
        val flights = mutableMapOf<Long, Flight>()
        private var nextId = 1L

        override fun getAll(): Flow<List<Flight>> = flowOf(flights.values.toList())
        override fun getByStatus(status: FlightStatus): Flow<List<Flight>> =
            flowOf(flights.values.filter { it.status == status })
        override suspend fun getById(id: Long): Flight? = flights[id]
        override suspend fun getActiveForAircraft(aircraftId: Long): List<Flight> =
            flights.values.filter { it.aircraftId == aircraftId && it.isActive }
        override suspend fun insert(f: Flight): Long {
            val id = nextId++
            flights[id] = f.copy(id = id)
            return id
        }
        override suspend fun update(f: Flight) { flights[f.id] = f }
    }

    private class FakeAirportRepository : AirportRepository {
        val airports = mutableMapOf<String, Airport>()

        override fun getAll(): Flow<List<Airport>> = flowOf(airports.values.toList())
        override suspend fun getByIata(iata: String): Airport? = airports[iata]
        override fun search(q: String): Flow<List<Airport>> =
            flowOf(airports.values.filter { it.iata.contains(q, ignoreCase = true) })
        override suspend fun seedIfEmpty(context: Context) {}
        override suspend fun count(): Int = airports.size
    }

    private class FakeContractRepository : ContractRepository {
        val contracts = mutableMapOf<Long, Contract>()
        private var nextId = 1L

        override fun getAvailable(): Flow<List<Contract>> =
            flowOf(contracts.values.filter { it.status == ContractStatus.AVAILABLE })
        override suspend fun getById(id: Long): Contract? = contracts[id]
        override suspend fun insert(c: Contract): Long {
            val id = nextId++
            contracts[id] = c.copy(id = id)
            return id
        }
        override suspend fun update(c: Contract) { contracts[c.id] = c }
        override suspend fun expireOld(currentGameMinutes: Long) {
            contracts.entries.removeAll { (_, c) ->
                c.status == ContractStatus.AVAILABLE && c.deadlineGameMinutes < currentGameMinutes
            }
        }
    }

    private class FakeGameStateRepository : GameStateRepository {
        var state: GameState? = null

        override fun get(): Flow<GameState?> = flowOf(state)
        override suspend fun insert(s: GameState) { state = s }
        override suspend fun update(s: GameState) { state = s }
    }

    private class FakeEmployeeRepository : EmployeeRepository {
        val employees = mutableMapOf<Long, Employee>()
        private var nextId = 1L

        override fun getAll(): Flow<List<Employee>> = flowOf(employees.values.toList())
        override suspend fun getById(id: Long): Employee? = employees[id]
        override fun getByType(type: EmployeeType): Flow<List<Employee>> =
            flowOf(employees.values.filter { it.type == type })
        override suspend fun insert(e: Employee): Long {
            val id = nextId++
            employees[id] = e.copy(id = id)
            return id
        }
        override suspend fun update(e: Employee) { employees[e.id] = e }
        override suspend fun delete(id: Long) { employees.remove(id) }
    }

    // -----------------------------------------------------------------------
    // Test data builders
    // -----------------------------------------------------------------------

    private fun buildModel(
        id: Int = 1,
        rangeKm: Int = 5000,
        speedKmh: Int = 500,
        category: AircraftCategory = AircraftCategory.CHARTER,
        passengerCapacity: Int = 8
    ) = AircraftModel(
        id = id,
        manufacturer = "Test",
        model = "T-100",
        category = category,
        passengerCapacity = passengerCapacity,
        rangeKm = rangeKm,
        cruiseSpeedKmh = speedKmh,
        fuelBurnLph = 50.0,
        purchasePriceCoins = 1_000_000L,
        leasingCostPerHourCoins = 500L,
        maintenanceCostPerHourCoins = 100L
    )

    private fun buildAircraft(
        id: Long = 1L,
        model: AircraftModel,
        condition: Int = 100,
        totalFlightHours: Double = 0.0,
        nextMaintenanceHours: Double = 100.0
    ) = OwnedAircraft(
        id = id,
        modelId = model.id,
        model = model,
        registrationCode = "PT-SKY",
        acquisitionType = AcquisitionType.PURCHASED,
        condition = condition,
        totalFlightHours = totalFlightHours,
        nextMaintenanceHours = nextMaintenanceHours
    )

    private fun buildAirport(iata: String, lat: Double = 0.0, lon: Double = 0.0) = Airport(
        id = iata.hashCode(),
        iata = iata,
        icao = "X$iata",
        name = "$iata Airport",
        city = iata,
        country = "BR",
        lat = lat,
        lon = lon,
        region = "SA",
        hasHelipad = false
    )

    private fun buildGameState(mode: GameMode = GameMode.FICTIONAL) = GameState(
        id = 1L,
        companyName = "TestAir",
        gameMode = mode,
        balanceCoins = 10_000_000L,
        reputation = 50,
        researchPoints = 0,
        currentGameMinutes = 480L,
        dayNumber = 1
    )

    // -----------------------------------------------------------------------
    // SUT setup
    // -----------------------------------------------------------------------

    private lateinit var ownedAircraftRepo: FakeOwnedAircraftRepository
    private lateinit var aircraftModelRepo: FakeAircraftModelRepository
    private lateinit var flightRepo: FakeFlightRepository
    private lateinit var airportRepo: FakeAirportRepository
    private lateinit var contractRepo: FakeContractRepository
    private lateinit var gameStateRepo: FakeGameStateRepository
    private lateinit var employeeRepo: FakeEmployeeRepository
    private lateinit var useCase: ScheduleFlightUseCase

    @Before
    fun setUp() {
        ownedAircraftRepo = FakeOwnedAircraftRepository()
        aircraftModelRepo = FakeAircraftModelRepository()
        flightRepo = FakeFlightRepository()
        airportRepo = FakeAirportRepository()
        contractRepo = FakeContractRepository()
        gameStateRepo = FakeGameStateRepository()
        employeeRepo = FakeEmployeeRepository()

        useCase = ScheduleFlightUseCase(
            ownedAircraftRepository = ownedAircraftRepo,
            aircraftModelRepository = aircraftModelRepo,
            flightRepository = flightRepo,
            airportRepository = airportRepo,
            contractRepository = contractRepo,
            gameStateRepository = gameStateRepo,
            employeeRepository = employeeRepo
        )
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    fun `scheduleFlight happy path returns Success with correct flight`() = runTest {
        // Arrange — short domestic route (~360 km), well within 5000 km range
        val model = buildModel(rangeKm = 5000, speedKmh = 300)
        val aircraft = buildAircraft(id = 1L, model = model)
        val origin = buildAirport("GRU", lat = -23.432, lon = -46.469)
        val destination = buildAirport("VCP", lat = -23.007, lon = -47.134)

        ownedAircraftRepo.aircraft[1L] = aircraft
        aircraftModelRepo.models[1] = model
        airportRepo.airports["GRU"] = origin
        airportRepo.airports["VCP"] = destination
        gameStateRepo.state = buildGameState(GameMode.FICTIONAL)

        val request = ScheduleFlightRequest(
            aircraftId = 1L,
            operationType = OperationType.AIRLINE,
            originIata = "GRU",
            destinationIata = "VCP",
            departureGameMinutes = 600L,
            ticketPricePerPax = 150L,
            passengerCount = 6
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Expected Success but got $result", result is UseCaseResult.Success)
        val flight = (result as UseCaseResult.Success).data
        assertEquals(FlightStatus.SCHEDULED, flight.status)
        assertEquals(OperationType.AIRLINE, flight.operationType)
        assertEquals("GRU", flight.originIata)
        assertEquals("VCP", flight.destinationIata)
        assertEquals(6 * 150L, flight.revenueCoins)
        assertTrue("arrivalGameMinutes should be > departureGameMinutes",
            flight.arrivalGameMinutes > flight.departureGameMinutes)
    }

    @Test
    fun `scheduleFlight returns Failure when aircraft does not exist`() = runTest {
        // Arrange — aircraft map is empty
        airportRepo.airports["GRU"] = buildAirport("GRU")
        airportRepo.airports["VCP"] = buildAirport("VCP")
        gameStateRepo.state = buildGameState()

        val request = ScheduleFlightRequest(
            aircraftId = 99L, // does not exist
            operationType = OperationType.AIRLINE,
            originIata = "GRU",
            destinationIata = "VCP",
            departureGameMinutes = 600L,
            ticketPricePerPax = 100L,
            passengerCount = 4
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Expected Failure but got $result", result is UseCaseResult.Failure)
        val message = (result as UseCaseResult.Failure).message
        assertTrue("Message should mention aircraft", message.contains("Aircraft", ignoreCase = true))
    }

    @Test
    fun `scheduleFlight returns Failure when route exceeds aircraft range`() = runTest {
        // Arrange — model has only 100 km range; GRU↔LAX is ~9800 km
        val model = buildModel(rangeKm = 100, speedKmh = 300)
        val aircraft = buildAircraft(id = 1L, model = model)
        val origin = buildAirport("GRU", lat = -23.432, lon = -46.469)
        val destination = buildAirport("LAX", lat = 33.942, lon = -118.408)

        ownedAircraftRepo.aircraft[1L] = aircraft
        aircraftModelRepo.models[1] = model
        airportRepo.airports["GRU"] = origin
        airportRepo.airports["LAX"] = destination
        gameStateRepo.state = buildGameState()

        val request = ScheduleFlightRequest(
            aircraftId = 1L,
            operationType = OperationType.AIRLINE,
            originIata = "GRU",
            destinationIata = "LAX",
            departureGameMinutes = 600L,
            ticketPricePerPax = 100L,
            passengerCount = 4
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Expected Failure but got $result", result is UseCaseResult.Failure)
        val message = (result as UseCaseResult.Failure).message
        assertTrue("Message should mention range or distance", message.contains("range", ignoreCase = true)
            || message.contains("distance", ignoreCase = true))
    }

    @Test
    fun `scheduleFlight returns Failure when aircraft has a schedule conflict`() = runTest {
        // Arrange — aircraft already has an active flight from minute 500 to 800
        val model = buildModel(rangeKm = 5000, speedKmh = 300)
        val aircraft = buildAircraft(id = 1L, model = model)
        val origin = buildAirport("GRU", lat = -23.432, lon = -46.469)
        val destination = buildAirport("VCP", lat = -23.007, lon = -47.134)

        ownedAircraftRepo.aircraft[1L] = aircraft
        aircraftModelRepo.models[1] = model
        airportRepo.airports["GRU"] = origin
        airportRepo.airports["VCP"] = destination
        gameStateRepo.state = buildGameState()

        // Pre-existing conflicting flight: departs 500, arrives 800
        val conflictingFlight = Flight(
            id = 42L,
            operationType = OperationType.AIRLINE,
            aircraftId = 1L,
            originIata = "GRU",
            destinationIata = "VCP",
            departureGameMinutes = 500L,
            arrivalGameMinutes = 800L,
            passengerCount = 4,
            revenueCoins = 400L,
            status = FlightStatus.SCHEDULED
        )
        flightRepo.flights[42L] = conflictingFlight

        // New request overlaps with the existing flight (departs 600, before existing arrives 800)
        val request = ScheduleFlightRequest(
            aircraftId = 1L,
            operationType = OperationType.AIRLINE,
            originIata = "GRU",
            destinationIata = "VCP",
            departureGameMinutes = 600L,
            ticketPricePerPax = 100L,
            passengerCount = 4
        )

        // Act
        val result = useCase(request)

        // Assert
        assertTrue("Expected Failure due to conflict but got $result", result is UseCaseResult.Failure)
        val message = (result as UseCaseResult.Failure).message
        assertTrue("Message should mention conflict", message.contains("conflict", ignoreCase = true))
    }
}
