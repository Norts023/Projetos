package com.skytycoon.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.skytycoon.app.domain.model.Flight
import com.skytycoon.app.domain.model.FlightStatus
import com.skytycoon.app.domain.model.OperationType

@Entity(tableName = "flights")
data class FlightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operationType: String,
    val aircraftId: Long,
    val originIata: String,
    val destinationIata: String,
    val departureGameMinutes: Long,
    val arrivalGameMinutes: Long,
    val passengerCount: Int,
    val revenueCoins: Long,
    val status: String,
    val assignedPilotId: Long?,
    val assignedCopilotId: Long?,
    val contractId: Long?
) {
    fun toDomain(): Flight = Flight(
        id = id,
        operationType = OperationType.entries.firstOrNull { it.name == operationType } ?: OperationType.AIRLINE,
        aircraftId = aircraftId,
        originIata = originIata,
        destinationIata = destinationIata,
        departureGameMinutes = departureGameMinutes,
        arrivalGameMinutes = arrivalGameMinutes,
        passengerCount = passengerCount,
        revenueCoins = revenueCoins,
        status = FlightStatus.entries.firstOrNull { it.name == status } ?: FlightStatus.SCHEDULED,
        assignedPilotId = assignedPilotId,
        assignedCopilotId = assignedCopilotId,
        contractId = contractId
    )

    companion object {
        fun fromDomain(flight: Flight): FlightEntity = FlightEntity(
            id = flight.id,
            operationType = flight.operationType.name,
            aircraftId = flight.aircraftId,
            originIata = flight.originIata,
            destinationIata = flight.destinationIata,
            departureGameMinutes = flight.departureGameMinutes,
            arrivalGameMinutes = flight.arrivalGameMinutes,
            passengerCount = flight.passengerCount,
            revenueCoins = flight.revenueCoins,
            status = flight.status.name,
            assignedPilotId = flight.assignedPilotId,
            assignedCopilotId = flight.assignedCopilotId,
            contractId = flight.contractId
        )
    }
}
