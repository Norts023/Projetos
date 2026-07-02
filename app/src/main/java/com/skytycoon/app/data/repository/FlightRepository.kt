package com.skytycoon.app.data.repository

import com.skytycoon.app.data.local.dao.FlightDao
import com.skytycoon.app.data.local.entity.FlightEntity
import com.skytycoon.app.domain.model.Flight
import com.skytycoon.app.domain.model.FlightStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface FlightRepository {
    fun getAll(): Flow<List<Flight>>
    fun getByStatus(status: FlightStatus): Flow<List<Flight>>
    suspend fun getById(id: Long): Flight?
    suspend fun getActiveForAircraft(aircraftId: Long): List<Flight>
    suspend fun insert(flight: Flight): Long
    suspend fun update(flight: Flight)
}

@Singleton
class FlightRepositoryImpl @Inject constructor(
    private val flightDao: FlightDao
) : FlightRepository {

    // Mirrors Flight.isActive: SCHEDULED, BOARDING, IN_FLIGHT
    private val activeStatusNames: List<String> = listOf(
        FlightStatus.SCHEDULED.name,
        FlightStatus.BOARDING.name,
        FlightStatus.IN_FLIGHT.name
    )

    override fun getAll(): Flow<List<Flight>> =
        flightDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override fun getByStatus(status: FlightStatus): Flow<List<Flight>> =
        flightDao.getByStatus(status.name).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: Long): Flight? = withContext(Dispatchers.IO) {
        flightDao.getById(id)?.toDomain()
    }

    override suspend fun getActiveForAircraft(aircraftId: Long): List<Flight> = withContext(Dispatchers.IO) {
        flightDao.getActiveForAircraft(aircraftId, activeStatusNames).map { it.toDomain() }
    }

    override suspend fun insert(flight: Flight): Long = withContext(Dispatchers.IO) {
        flightDao.insert(FlightEntity.fromDomain(flight))
    }

    override suspend fun update(flight: Flight) = withContext(Dispatchers.IO) {
        flightDao.update(FlightEntity.fromDomain(flight))
    }
}
